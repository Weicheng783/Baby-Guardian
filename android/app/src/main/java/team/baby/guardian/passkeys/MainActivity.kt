package team.baby.guardian.passkeys

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import team.baby.guardian.passkeys.HomeFragment.HomeFragmentCallback
import team.baby.guardian.passkeys.MainFragment.MainFragmentCallback
import team.baby.guardian.R.id
import team.baby.guardian.passkeys.SignInFragment.SignInFragmentCallback
import team.baby.guardian.passkeys.SignUpFragment.SignUpFragmentCallback
import team.baby.guardian.databinding.ActivityMainBinding

class BabyPasskeysActivity : AppCompatActivity(), MainFragmentCallback, HomeFragmentCallback,
    SignInFragmentCallback, SignUpFragmentCallback {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        DataProvider.initSharedPref(applicationContext)

        if (DataProvider.isSignedIn()) {
            showHome()
        } else {
            loadMainFragment()
        }
    }

    override fun signup() {
        loadFragment(SignUpFragment(), false)
    }

    override fun signIn() {
        loadFragment(SignInFragment(), false)
    }

    override fun logout() {
        supportFragmentManager.popBackStack("home", FragmentManager.POP_BACK_STACK_INCLUSIVE)
        loadMainFragment()
    }

    private fun loadMainFragment() {
        supportFragmentManager.popBackStack()
        loadFragment(MainFragment(), false)
    }

    override fun showHome() {
        supportFragmentManager.popBackStack()
        loadFragment(HomeFragment(), true, "home")
    }

    private fun loadFragment(fragment: Fragment, flag: Boolean, backstackString: String? = null) {
        DataProvider.configureSignedInPref(flag)
        supportFragmentManager.beginTransaction().replace(id.fragment_container, fragment)
            .addToBackStack(backstackString).commit()
    }

    override fun onBackPressed() {
        if (DataProvider.isSignedIn() || supportFragmentManager.backStackEntryCount == 1) {
            finish()
        } else {
            super.onBackPressed()
        }
    }
}