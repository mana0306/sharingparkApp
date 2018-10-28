package com.lzj.its.sharingpark.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.allen.library.SuperTextView;
import com.lzj.its.sharingpark.R;
import com.lzj.its.sharingpark.activity.AccountActivity;
import com.lzj.its.sharingpark.activity.LoginActivity;
import com.lzj.its.sharingpark.activity.MainActivity;
import com.lzj.its.sharingpark.util.SharedPreferencesUtils;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link UserFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UserFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";

    // TODO: Rename and change types of parameters
    private String mParam1;

    public UserFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment UserFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static UserFragment newInstance(String param1) {
        UserFragment fragment = new UserFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_user, container, false);
//        TextView textView = view.findViewById(R.id.textView);
//        textView.setText(mParam1);
        SuperTextView stv_logout = view.findViewById(R.id.stv_logout);
        stv_logout.setOnSuperTextViewClickListener(superTextView1 -> {
            //置空密码即可
            //获取SharedPreferences对象，使用自定义类的方法来获取对象
            SharedPreferencesUtils helper = new SharedPreferencesUtils(getActivity(), "setting");
            //创建记住密码和自动登录是默认不选,密码为空
            helper.putValues(
                    new SharedPreferencesUtils.ContentValue("autoLogin", false));
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
        });

        SuperTextView stv_account = view.findViewById(R.id.stv_account);
        stv_account.setOnSuperTextViewClickListener(superTextView -> {
           Intent intent = new Intent(getActivity(), AccountActivity.class);
           startActivity(intent);
        });

        return view;
    }

}
