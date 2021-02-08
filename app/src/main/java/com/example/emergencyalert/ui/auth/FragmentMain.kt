package com.example.emergencyalert.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.emergencyalert.R
import com.example.emergencyalert.data.repository.AuthRepository
import com.example.emergencyalert.data.repository.MOBILE_NUMBER
import com.example.emergencyalert.data.repository.UtilSharedPreferences
import com.example.emergencyalert.ui.mainscreen.DashBoardActivity
import kotlinx.android.synthetic.main.fragment_main.*

class FragmentMain : Fragment() {

    var navController:NavController?=null;
    var editText:EditText?=null;
    var button:Button?=null;
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(UtilSharedPreferences.getLoggedIn(requireContext())){
            var intent=Intent(context, DashBoardActivity::class.java);
            startActivity(intent)
            requireActivity()!!.finish()
            return
        }
        navController=Navigation.findNavController(view);
        button=view.findViewById(R.id.btn_continue);
        editText=view.findViewById(R.id.edt_mobile);

        button!!.setOnClickListener {
            if(editText!!.text.toString().isNullOrEmpty()||editText!!.text.toString().length<10){
                Toast.makeText(requireContext(), "Invalid Phone Number !", Toast.LENGTH_SHORT).show();
                return@setOnClickListener
            }
            MOBILE_NUMBER=editText!!.text.toString();
            register_Progress.visibility=View.VISIBLE;
            val loginresponse=AuthRepository().sendOtp(editText?.text.toString());
            loginresponse.observe(viewLifecycleOwner, Observer {
                Log.d("RESPONSE", "onViewCreated: "+loginresponse.value)
                register_Progress.visibility=View.INVISIBLE;
                navController!!.navigate(R.id.action_fragmentMain_to_fragmentOTP);
            })
        }
    }

}