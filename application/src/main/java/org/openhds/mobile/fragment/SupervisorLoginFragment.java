package org.openhds.mobile.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.apache.http.HttpStatus;
import org.openhds.mobile.R;
import org.openhds.mobile.activity.OpeningActivity;
import org.openhds.mobile.activity.SupervisorMainActivity;
import org.openhds.mobile.model.core.Supervisor;
import org.openhds.mobile.provider.DatabaseAdapter;
import org.openhds.mobile.task.SupervisorLoginTask;
import org.openhds.mobile.task.http.HttpTask;
import org.openhds.mobile.task.http.HttpTaskRequest;
import org.openhds.mobile.task.http.HttpTaskResponse;

import static org.openhds.mobile.utilities.ConfigUtils.getResourceString;
import static org.openhds.mobile.utilities.MessageUtils.showLongToast;
import static org.openhds.mobile.utilities.UrlUtils.buildServerUrl;

public class SupervisorLoginFragment extends Fragment implements OnClickListener {

    private EditText usernameEditText;
    private EditText passwordEditText;
    private String username;
    private String password;
    private DatabaseAdapter databaseAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.generic_login_fragment, container, false);
        TextView title = (TextView) v.findViewById(R.id.titleTextView);
        title.setText(R.string.supervisor_login);

        usernameEditText = (EditText) v.findViewById(R.id.usernameEditText);
        passwordEditText = (EditText) v.findViewById(R.id.passwordEditText);
        Button loginButton = (Button) v.findViewById(R.id.loginButton);
        loginButton.setOnClickListener(this);

        databaseAdapter = new DatabaseAdapter(getActivity());

        return v;
    }

    @Override
    public void onClick(View view) {
        // copy credentials to use until next login attempt
        username = usernameEditText.getText().toString();
        password = passwordEditText.getText().toString();

        authenticateSupervisor();
    }

    private String getLoginUrl() {
        String path = getResourceString(getActivity(), R.string.supervisor_login_url);
        return buildServerUrl(getActivity(), path);
    }

    private void authenticateSupervisor() {
        HttpTaskRequest httpTaskRequest = new HttpTaskRequest(
                R.string.login_btn,
                getLoginUrl(),
                "application/xml",
                username,
                password);

        HttpTask httpTask = new HttpTask(new AuthenticateListener());
        httpTask.execute(httpTaskRequest);
    }

    private void onConnectedAndAuthenticated(HttpTaskResponse httpTaskResponse) {
        // valid credentials were cached in tablet database by AuthenticateTask
        // delete any stale credentials from local then add authenticated credentials
        deleteSupervisor();
        addSupervisor();
        launchSupervisorMainActivity();
    }

    private void onConnectedButNotAuthenticated() {
        // delete unauthorized user from tablet database
        // to prevent login when not connected to network
        showLongToast(getActivity(), R.string.supervisor_bad_credentials);
        deleteSupervisor();
    }

    private void deleteSupervisor() {
        Supervisor user = new Supervisor();
        user.setName(password);
        databaseAdapter.deleteSupervisor(user);
    }

    private void addSupervisor() {
        Supervisor user = new Supervisor();
        user.setName(username);
        user.setPassword(password);
        databaseAdapter.addSupervisor(user);
    }

    private void onNotConnected() {
        // attempt to log in using cached credentials in tablet database
        SupervisorLoginTask loginTask = new SupervisorLoginTask(
                databaseAdapter,
                username,
                password,
                new LoginListener());
        loginTask.execute();
    }

    private void launchSupervisorMainActivity() {
        Intent intent = new Intent(getActivity(), SupervisorMainActivity.class);
        intent.putExtra(OpeningActivity.USERNAME_KEY, username);
        intent.putExtra(OpeningActivity.PASSWORD_KEY, password);
        startActivity(intent);
    }

    private class AuthenticateListener implements HttpTask.HttpTaskResponseHandler {
        @Override
        public void handleHttpTaskResponse(HttpTaskResponse httpTaskResponse) {
            if (httpTaskResponse.isSuccess()) {
                onConnectedAndAuthenticated(httpTaskResponse);
                return;
            }

            if (HttpStatus.SC_FORBIDDEN == httpTaskResponse.getHttpStatus()) {
                onConnectedButNotAuthenticated();
                return;
            }

            onNotConnected();
        }
    }

    private class LoginListener implements SupervisorLoginTask.Listener {
        public void onAuthenticated() {
            launchSupervisorMainActivity();
        }

        public void onBadAuthentication() {
            showLongToast(getActivity(), R.string.supervisor_bad_credentials);
        }
    }
}
