package ca.tourguide.ui.account

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.credentials.CredentialManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import ca.tourguide.BuildConfig
import ca.tourguide.databinding.FragmentAccountBinding
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import coil.load
import com.google.firebase.auth.GoogleAuthProvider
import ca.tourguide.R
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException

class AccountFragment : Fragment() {

    private var _binding: FragmentAccountBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var credentialManager: CredentialManager
    private lateinit var auth: FirebaseAuth
    private lateinit var accountViewModel: AccountViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccountBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        credentialManager = CredentialManager.create(requireContext())
        auth = FirebaseAuth.getInstance()

        accountViewModel =
            ViewModelProvider(this)[AccountViewModel::class.java]

        accountViewModel.title.observe(viewLifecycleOwner) {
            binding.textAccount.text = it
        }

        accountViewModel.isLoggedIn.observe(viewLifecycleOwner) { loggedIn ->
            binding.btnSignIn.visibility = if (loggedIn) View.GONE else View.VISIBLE
            binding.btnSignOut.visibility = if (loggedIn) View.VISIBLE else View.GONE
            binding.textEmail.visibility = if (loggedIn) View.VISIBLE else View.GONE
            binding.imageProfile.visibility = if (loggedIn) View.VISIBLE else View.GONE
        }

        accountViewModel.userInfo.observe(viewLifecycleOwner) { user ->
            binding.textEmail.text = user?.email ?: ""
            binding.imageProfile.load(user?.photoUri) {
                crossfade(true)
                placeholder(R.drawable.ic_profile_24dp)
                error(R.drawable.ic_profile_24dp)
            }
        }

        binding.btnSignIn.setOnClickListener {
            lifecycleScope.launch {
                startManualGoogleSignIn()
            }
        }

        binding.btnSignOut.setOnClickListener {
            auth.signOut()
            accountViewModel.updateLoginState(null)
        }

        auth.currentUser?.let {
            accountViewModel.updateLoginState(it)
        }
    }

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account?.idToken
                if (!idToken.isNullOrEmpty()) {
                    val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                    auth.signInWithCredential(firebaseCredential)
                        .addOnSuccessListener {
                            accountViewModel.updateLoginState(auth.currentUser)
                            Log.d("AUTH", "Account status updated")
                        }
                        .addOnFailureListener {
                            Log.e("AUTH", "Fallback Firebase sign-in failed", it)
                            Toast.makeText(requireContext(), "Authentication failed", Toast.LENGTH_SHORT).show()
                        }
                }
            } catch (e: ApiException) {
                Log.e("AUTH", "Fallback sign-in failed: ${e.statusCode}", e)
            }
        } else{
            Log.e("Auth", "Backup login failed")
            Log.e("Auth", result.toString())
        }
    }

    private fun startManualGoogleSignIn(){
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(BuildConfig.GOOGLE_CLIENT_ID)
            .requestEmail()
            .build()

        val client = GoogleSignIn.getClient(requireContext(), gso)
        Log.d("Auth", "Launching backup sign in")
        googleSignInLauncher.launch(client.signInIntent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}