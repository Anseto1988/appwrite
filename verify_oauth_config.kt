// Quick verification script to ensure OAuth callback URLs are correct
// This can be run to verify the configuration

fun main() {
    // Expected values
    val projectId = "snackrack2"
    val expectedCallbackUrl = "appwrite-callback-snackrack2://auth"
    
    println("=== OAuth Configuration Verification ===")
    println("")
    println("Expected Configuration:")
    println("- Project ID: $projectId")
    println("- Callback URL: $expectedCallbackUrl")
    println("")
    println("AndroidManifest.xml should have:")
    println("- Scheme: appwrite-callback-snackrack2")
    println("")
    println("AuthRepository will construct:")
    println("- Success URL: appwrite-callback-\${project}://auth")
    println("- Failure URL: appwrite-callback-\${project}://auth")
    println("- Which resolves to: $expectedCallbackUrl")
    println("")
    println("Google Console redirect URIs needed:")
    println("1. https://parse.nordburglarp.de/v1/account/sessions/oauth2/callback/google/snackrack2")
    println("2. appwrite-callback-snackrack2://auth")
}