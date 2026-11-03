package fr.spamoff

import android.app.role.RoleManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.spamoff.ui.MainViewModel
import java.text.DateFormat
import java.util.Date

class MainActivity: ComponentActivity() {
 override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState); enableEdgeToEdge()
  setContent {
   val vm: MainViewModel = viewModel()
   val settings by vm.settings.collectAsStateWithLifecycle()
   val dark = when(settings.theme) { "Sombre" -> true; "Clair" -> false; else -> isSystemInDarkTheme() }
   MaterialTheme(colorScheme=if(dark) darkColorScheme(primary=Color(0xFF68DBCA)) else lightColorScheme(primary=Color(0xFF007E70))) {
    Surface(Modifier.fillMaxSize()) { App(vm) }
   }
  }
 }
 @Composable private fun App(vm: MainViewModel) {
  var page by rememberSaveableCompat { mutableIntStateOf(0) }
  val role = getSystemService(RoleManager::class.java)
  fun granted() = role.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING) && role.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)
  var hasRole by remember { mutableStateOf(granted()) }
  val request = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { hasRole = granted() }
  val owner = LocalLifecycleOwner.current
  DisposableEffect(owner) {
   val observer = LifecycleEventObserver { _, event -> if(event == Lifecycle.Event.ON_RESUME) hasRole = granted() }
   owner.lifecycle.addObserver(observer); onDispose { owner.lifecycle.removeObserver(observer) }
  }
  val s by vm.settings.collectAsStateWithLifecycle()
  val counts by vm.counts.collectAsStateWithLifecycle()
  val metadata by vm.metadata.collectAsStateWithLifecycle()
  val rejected by vm.rejected.collectAsStateWithLifecycle()
  val people by vm.people.collectAsStateWithLifecycle()
  val busy by vm.busy.collectAsStateWithLifecycle()
  val message by vm.message.collectAsStateWithLifecycle()
  var info by remember { mutableStateOf<String?>(null) }
  val snackbar = remember { SnackbarHostState() }
  LaunchedEffect(message) { message?.let { snackbar.showSnackbar(it); vm.clearMessage() } }
  Scaffold(snackbarHost={ SnackbarHost(snackbar) }, bottomBar={
   NavigationBar { listOf("Accueil", "Signaler", "Listes", "Paramètres").forEachIndexed { i, label ->
    NavigationBarItem(selected=page == i, onClick={ page=i }, icon={ Text(listOf("◉", "+", "≡", "⚙")[i]) }, label={ Text(label, maxLines=1) })
   } }
  }) { padding ->
   Column(Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement=Arrangement.spacedBy(16.dp)) {
    Text("SpamOff", style=MaterialTheme.typography.headlineLarge, fontWeight=FontWeight.Bold)
    Text(listOf("Votre tranquillité, simplement.", "Un numéro indésirable ?", "Vos règles, votre choix.", "Une protection à votre mesure.")[page], style=MaterialTheme.typography.bodyLarge)
    when(page) {
     0 -> {
      Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(20.dp), verticalArrangement=Arrangement.spacedBy(12.dp)) {
       Text(if(s.enabled && hasRole) "Protection activée" else "Protection inactive", style=MaterialTheme.typography.headlineSmall)
       Toggle("Activer le filtrage", s.enabled, vm::enabled)
       Text(if(hasRole) "Rôle Android : accordé" else "Sélectionnez SpamOff comme application de filtrage des appels.")
       if(!hasRole) Button(onClick={ if(role.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING)) request.launch(role.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)) else info="Le rôle de filtrage n’est pas disponible sur cet appareil." }, modifier=Modifier.fillMaxWidth()) { Text("Autoriser dans Android") }
      } }
      Text("${counts?.exact ?: 0} numéros exacts · ${counts?.prefixes ?: 0} préfixes", style=MaterialTheme.typography.titleLarge)
      Text("${people.count { !it.allowed }} numéros bloqués personnellement · ${people.count { it.allowed }} autorisés")
      Text("${rejected ?: 0} demandes de blocage envoyées à Android")
      Text("Ce compteur local mesure les décisions transmises, pas une confirmation du rejet par l’opérateur.", style=MaterialTheme.typography.bodySmall)
      Text("Dernière actualisation : " + (metadata?.updated?.takeIf { it > 0 }?.let { DateFormat.getDateTimeInstance().format(Date(it)) } ?: "base initiale embarquée"))
      Button(onClick=vm::update, enabled=!busy, modifier=Modifier.fillMaxWidth()) { Text(if(busy) "Vérification…" else "Actualiser les listes") }
      AutomatedToggle(s.automated, vm::automated)
      Text("Les appels masqués et certains appels ne sont pas transmis à l’application. Sans permission Contacts, Android exempte normalement vos contacts.", style=MaterialTheme.typography.bodySmall)
     }
     1 -> ReportForm(vm)
     2 -> {
      var number by remember { mutableStateOf("") }
      var search by remember { mutableStateOf("") }
      OutlinedTextField(value=number, onValueChange={ if(it.length<=80) number=it }, label={ Text("Numéro (+indicatif hors France)") }, keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Phone), singleLine=true, modifier=Modifier.fillMaxWidth())
      Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
       Button(onClick={ vm.add(number,true) }, modifier=Modifier.weight(1f)) { Text("Autoriser") }
       OutlinedButton(onClick={ vm.add(number,false) }, modifier=Modifier.weight(1f)) { Text("Bloquer") }
      }
      Text("La liste blanche est toujours prioritaire. Un numéro ne figure que dans une liste personnelle à la fois.")
      OutlinedTextField(value=search, onValueChange={ search=it }, label={ Text("Rechercher dans vos numéros") }, modifier=Modifier.fillMaxWidth())
      val filtered = people.filter { search.isBlank() || it.number.contains(search) }
      if(filtered.isEmpty()) Text("Aucun numéro personnel à afficher.")
      filtered.take(200).forEach { person ->
       Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(12.dp)) {
        Text(person.number, style=MaterialTheme.typography.titleMedium)
        Text(if(person.allowed) "Autorisé" else "Bloqué")
        TextButton(onClick={ vm.remove(person.number) }) { Text("Supprimer") }
       } }
      }
      if(filtered.size > 200) Text("Précisez votre recherche pour afficher les autres numéros.")
      Text("Base collective : ${counts?.exact ?: 0} numéros exacts et ${counts?.prefixes ?: 0} préfixes. Aucun flux communautaire n’est activé dans cette V1.")
     }
     3 -> {
      Toggle("Activer le filtrage",s.enabled,vm::enabled)
      AutomatedToggle(s.automated,vm::automated)
      Toggle("Actualisation automatique quotidienne",s.autoUpdate,vm::auto)
      Text("Android choisit l’horaire selon le réseau et la batterie. Aucune connexion n’est nécessaire pour filtrer.")
      Text("Apparence", style=MaterialTheme.typography.titleMedium)
      Row(horizontalArrangement=Arrangement.spacedBy(6.dp)) { listOf("Système","Clair","Sombre").forEach { value -> FilterChip(selected=s.theme==value,onClick={vm.theme(value)},label={Text(value)}) } }
      OutlinedButton(onClick={page=2},modifier=Modifier.fillMaxWidth()) { Text("Gérer mes numéros") }
      TextButton(onClick={info="Sources : ARCEP, plan de numérotation, catégorie numéros polyvalents vérifiés. Transcription du 03/11/2026 : 22 préfixes internationaux. Ces plages peuvent aussi servir à des appels légitimes. Source : https://www.arcep.fr/la-regulation/grands-dossiers-thematiques-transverses/la-numerotation.html\n\nMises à jour : dépôt public cortexAI-2025/SpamOff, répertoire data. Contrôle SHA-256 et remplacement atomique. SHA-256 détecte une corruption, pas une compromission de l’éditeur. Aucun fichier de tiers redistribué. Bibliothèques : AndroidX, Kotlin et libphonenumber (Apache 2.0), Retrofit/OkHttp (Apache 2.0)."}) { Text("Sources et licences") }
      TextButton(onClick={info="SpamOff traite les numéros sur ce téléphone. Aucun compte, aucune publicité, aucune analytique. Aucun accès aux contacts, aux SMS, au micro ou au journal d’appels. Les signalements et commentaires restent locaux ; aucune API communautaire n’est activée. Supprimer un numéro personnel supprime aussi son signalement associé. Effacez les données de l’application pour tout supprimer. Les sauvegardes Android de l’application sont désactivées.\n\nLes mises à jour téléchargent uniquement des fichiers HTTPS publics sur GitHub, qui reçoit les données réseau habituelles (dont l’adresse IP). Aucun numéro ni historique n’est envoyé. Vous pouvez désactiver ces mises à jour. Le compteur n’enregistre aucun numéro ni horaire d’appel. Les restrictions du système et de l’opérateur s’appliquent. Version bêta 0.1.0."}) { Text("Confidentialité") }
      Text("SpamOff 0.1.0 · Gratuit · Sans publicité",style=MaterialTheme.typography.bodySmall)
     }
    }
    Spacer(Modifier.height(12.dp))
   }
  }
  info?.let { text -> AlertDialog(onDismissRequest={info=null},confirmButton={TextButton(onClick={info=null}) { Text("Fermer") }},title={Text("SpamOff")},text={Text(text,modifier=Modifier.verticalScroll(rememberScrollState()))}) }
 }
}
@Composable private fun Toggle(label: String, checked: Boolean, change: (Boolean)->Unit) {
 Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.SpaceBetween, verticalAlignment=androidx.compose.ui.Alignment.CenterVertically) {
  Text(label,Modifier.weight(1f).padding(end=12.dp)); Switch(checked=checked,onCheckedChange=change)
 }
}
@Composable private fun AutomatedToggle(value: Boolean, change: (Boolean)->Unit) {
 Column { Toggle("Bloquer les plages d’appels automatisés",value,change)
  Text("Option ARCEP : peut bloquer des appels légitimes. Autorisez les numéros à conserver dans votre liste blanche.",style=MaterialTheme.typography.bodySmall)
 }
}
@Composable private fun ReportForm(vm: MainViewModel) {
 var number by remember { mutableStateOf("") }
 var category by remember { mutableStateOf("Démarchage") }
 var comment by remember { mutableStateOf("") }
 var block by remember { mutableStateOf(true) }
 OutlinedTextField(value=number,onValueChange={if(it.length<=80) number=it},label={Text("Numéro")},singleLine=true,keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Phone),modifier=Modifier.fillMaxWidth())
 listOf("Démarchage","Fraude","Robot vocal","Spam","Autre").forEach { name ->
  Row(verticalAlignment=androidx.compose.ui.Alignment.CenterVertically) { RadioButton(selected=category==name,onClick={category=name}); Text(name) }
 }
 OutlinedTextField(value=comment,onValueChange={if(it.length<=500) comment=it},label={Text("Commentaire facultatif (${comment.length}/500)")},modifier=Modifier.fillMaxWidth())
 Toggle("Ajouter à ma liste de blocage",block,{block=it})
 Button(onClick={vm.report(number,category,comment,block)},modifier=Modifier.fillMaxWidth()) { Text("Enregistrer sur ce téléphone") }
 Text("Ce signalement reste privé et local. Il n’est envoyé à aucun serveur.")
}
// Save only navigation state, never telephone numbers in saved instance state.
@Composable private fun rememberSaveableCompat(init: ()->androidx.compose.runtime.MutableIntState) = androidx.compose.runtime.saveable.rememberSaveable { init() }
