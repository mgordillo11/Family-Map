package com.example.family_map;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import RequestResponse.LoginRequest;
import RequestResponse.RegisterRequest;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class LoginFragment extends Fragment {
    private EditText editTextServerHost;
    private EditText editTextServerPort;
    private EditText editTextUsername;
    private EditText editTextPassword;
    private EditText editTextFirstName;
    private EditText editTextLastName;
    private EditText editTextEmail;
    private RadioGroup genderButtons;
    private Button registerButton;
    private Button signInButton;
    RadioButton maleButton;
    RadioButton femaleButton;

    private Listener listener;

    public interface Listener {
        void notifyDone();
    }

    public void loginRegisterListener(Listener listener) {
        this.listener = listener;
    }

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        editTextServerHost = view.findViewById(R.id.serverHostInput);
        editTextServerPort = view.findViewById(R.id.serverPortInput);
        editTextUsername = view.findViewById(R.id.userNameInput);
        editTextPassword = view.findViewById(R.id.passwordInput);
        editTextFirstName = view.findViewById(R.id.firstNameInput);
        editTextLastName = view.findViewById(R.id.lastNameInput);
        editTextEmail = view.findViewById(R.id.emailInput);

        genderButtons = view.findViewById(R.id.maleFemaleButtons);
        maleButton = view.findViewById(R.id.maleButton);
        femaleButton = view.findViewById(R.id.femaleButton);

        registerButton = view.findViewById(R.id.registerButton);
        signInButton = view.findViewById(R.id.signInButton);

        editTextServerHost.addTextChangedListener(loginTextWatcher);
        editTextServerPort.addTextChangedListener(loginTextWatcher);
        editTextUsername.addTextChangedListener(loginTextWatcher);
        editTextPassword.addTextChangedListener(loginTextWatcher);
        editTextFirstName.addTextChangedListener(loginTextWatcher);
        editTextLastName.addTextChangedListener(loginTextWatcher);
        editTextEmail.addTextChangedListener(loginTextWatcher);

        genderButtons.setOnCheckedChangeListener((genderButtons, i) -> listenToLoginViewAndButtons());

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Handler UIThreadHandler = new Handler() {
                    @Override
                    public void handleMessage(Message message) {
                        Bundle registerBundle = message.getData();
                        Toast registerToast = Toast.makeText(view.getContext(), registerBundle.getString("message"), Toast.LENGTH_SHORT);
                        registerToast.show();

                        if (registerBundle.getBoolean("status")) {
                            DataCache.getInstance().userLoggedIn = true;
                            listener.notifyDone();
                        }
                    }
                };

                String genderChoice;

                if (genderButtons.findViewById(genderButtons.getCheckedRadioButtonId()) == maleButton) {
                    genderChoice = "m";
                } else {
                    genderChoice = "f";
                }

                RegisterRequest userRegisterRequest = new RegisterRequest(editTextUsername.getText().toString(), editTextPassword.getText().toString(),
                        editTextEmail.getText().toString(), editTextFirstName.getText().toString(), editTextLastName.getText().toString(), genderChoice);

                RegisterTask registerTask = new RegisterTask(userRegisterRequest, editTextServerHost.getText().toString(), editTextServerPort.getText().toString(), UIThreadHandler);

                ExecutorService executorService = Executors.newSingleThreadExecutor();
                executorService.submit(registerTask);
            }
        });

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Handler UIThreadHandler = new Handler() {
                    @Override
                    public void handleMessage(Message message) {
                        Bundle loginBundle = message.getData();
                        Toast registerToast = Toast.makeText(view.getContext(), loginBundle.getString("message"), Toast.LENGTH_SHORT);
                        registerToast.show();

                        if (loginBundle.getBoolean("status")) {
                            DataCache.getInstance().userLoggedIn = true;
                            listener.notifyDone();
                        }
                    }
                };

                LoginRequest loginRequest = new LoginRequest(editTextUsername.getText().toString(), editTextPassword.getText().toString());

                LoginTask loginTask = new LoginTask(loginRequest, editTextServerHost.getText().toString(), editTextServerPort.getText().toString(), UIThreadHandler);

                ExecutorService executorService = Executors.newSingleThreadExecutor();
                executorService.submit(loginTask);
            }
        });
        return view;
    }

    private static class RegisterTask implements Runnable {
        private final Handler registerHandler;
        private final RegisterRequest registerRequest;
        private final String serverHost;
        private final String serverPort;

        public RegisterTask(RegisterRequest registerRequest, String serverHost, String serverPort, Handler handler) {
            this.registerHandler = handler;
            this.registerRequest = registerRequest;
            this.serverHost = serverHost;
            this.serverPort = serverPort;
        }

        @Override
        public void run() {
            boolean responseStatus = ServerProxy.postRegisterUser(serverHost, serverPort, registerRequest);
            sendMessage(responseStatus);
        }

        private void sendMessage(boolean currentStatus) {
            Message message = Message.obtain();
            Bundle messageBundle = new Bundle();
            messageBundle.putBoolean("status", currentStatus);

            if (currentStatus) {
                messageBundle.putString("message", "Welcome " + DataCache.getInstance().getCurrentPerson().getFirstName() + " "
                        + DataCache.getInstance().getCurrentPerson().getLastName());
            } else {
                messageBundle.putString("message", "User already exists!");
            }

            message.setData(messageBundle);
            registerHandler.sendMessage(message);
        }
    }

    private static class LoginTask implements Runnable {
        private final Handler loginHandler;
        private final LoginRequest loginRequest;
        private final String serverHost;
        private final String serverPort;

        public LoginTask(LoginRequest loginRequest, String serverHost, String serverPort, Handler handler) {
            this.loginHandler = handler;
            this.serverHost = serverHost;
            this.serverPort = serverPort;
            this.loginRequest = loginRequest;
        }

        @Override
        public void run() {
            boolean loginStatus = ServerProxy.postLoginUser(serverHost, serverPort, loginRequest);
            sendMessage(loginStatus);
        }

        private void sendMessage(boolean currentStatus) {
            Message message = Message.obtain();
            Bundle messageBundle = new Bundle();
            String firstName = DataCache.getInstance().getCurrentPerson().getFirstName();
            String lastName = DataCache.getInstance().getCurrentPerson().getLastName();

            messageBundle.putBoolean("status", currentStatus);

            if (currentStatus) {
                messageBundle.putString("message", "Welcome " + firstName + " "
                        + lastName);
            } else {
                messageBundle.putString("message", "Invalid Login Inputs");
            }

            message.setData(messageBundle);
            loginHandler.sendMessage(message);
        }
    }

    private final TextWatcher loginTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            listenToLoginViewAndButtons();
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    private void listenToLoginViewAndButtons() {
        String serverHostInput = editTextServerHost.getText().toString().trim();
        String serverPortInput = editTextServerPort.getText().toString().trim();
        String usernameInput = editTextUsername.getText().toString().trim();
        String passwordInput = editTextPassword.getText().toString().trim();
        String firstnameInput = editTextFirstName.getText().toString().trim();
        String lastNameInput = editTextLastName.getText().toString().trim();
        String emailInput = editTextEmail.getText().toString().trim();

        boolean genderSelected = (genderButtons.getCheckedRadioButtonId() != -1);

        boolean registerEnabled = (!serverHostInput.isEmpty()
                && !serverPortInput.isEmpty() && !usernameInput.isEmpty() && !passwordInput.isEmpty()
                && !firstnameInput.isEmpty() && !lastNameInput.isEmpty() && !emailInput.isEmpty() && genderSelected);

        boolean signInEnabled = (!serverHostInput.isEmpty()
                && !serverPortInput.isEmpty() && !usernameInput.isEmpty() && !passwordInput.isEmpty());

        registerButton.setEnabled(registerEnabled);
        signInButton.setEnabled(signInEnabled);
    }
}