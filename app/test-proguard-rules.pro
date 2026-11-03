# AndroidX Test references these compile-time-only Error Prone annotations.
# They have no runtime behavior; do not suppress missing application classes.
-dontwarn com.google.errorprone.annotations.CanIgnoreReturnValue
-dontwarn com.google.errorprone.annotations.MustBeClosed
