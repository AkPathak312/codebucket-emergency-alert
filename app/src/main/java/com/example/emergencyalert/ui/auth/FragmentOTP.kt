package com.example.emergencyalert.ui.auth

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.lifecycle.observe
import com.example.emergencyalert.R
import com.example.emergencyalert.data.repository.AuthRepository
import com.example.emergencyalert.data.repository.MOBILE_NUMBER
import com.example.emergencyalert.data.repository.UtilSharedPreferences
import com.example.emergencyalert.ui.mainscreen.DashBoardActivity
import com.mukesh.OtpView
import kotlinx.android.synthetic.main.fragment_o_t_p.*
import org.json.JSONObject
import java.util.Observer


class FragmentOTP : Fragment() {
    var otpView: OtpView? = null;
    var button: Button? = null;
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_o_t_p, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        button = view.findViewById(R.id.verifyotp);
        otpView = view.findViewById(R.id.otpViewConfirm);
        button!!.setOnClickListener {
            verifyOTP()
        }
    }

    private fun verifyOTP() {
        verify_progress.visibility = View.VISIBLE;
        val mobile: String? = MOBILE_NUMBER;
        val otp = otpView!!.text.toString();
        val response = AuthRepository().VerifyOTP(mobile!!, otp);
        response.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            Log.d("verifyOTP", "verifyOTP: " + response.value)
            verify_progress.visibility = View.INVISIBLE;
            try {
                val jsonObject = JSONObject(it);
                var token = jsonObject.getString("token");
                UtilSharedPreferences.setAuthToken(requireContext(),token);
                UtilSharedPreferences.setLoggedIn(requireContext(),true);
                val intent= Intent(context,DashBoardActivity::class.java);
                startActivity(intent)
                requireActivity().finish()
            } catch (exception: Exception) {
                Toast.makeText(context, "Verification Failed", Toast.LENGTH_SHORT).show()

                // navController!!.navigate(R.id.action_fragmentOtpVerification_to_fragmentOtpConfirmation)
            }
        })
    }

}