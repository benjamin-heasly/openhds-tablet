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
import org.openhds.mobile.links.Link;
import org.openhds.mobile.links.ResourceLinkRegistry;
import org.openhds.mobile.model.core.User;
import org.openhds.mobile.repository.GatewayRegistry;
import org.openhds.mobile.repository.gateway.UserGateway;
import org.openhds.mobile.task.http.HttpTask;
import org.openhds.mobile.task.http.HttpTaskRequest;
import org.openhds.mobile.task.http.HttpTaskResponse;
import org.openhds.mobile.task.parsing.entities.ParseLinksTask;

import java.util.Map;
import java.util.UUID;

import static org.openhds.mobile.utilities.ConfigUtils.getResourceString;
import static org.openhds.mobile.utilities.MessageUtils.showLongToast;
import static org.openhds.mobile.utilities.UrlUtils.buildServerUrl;

public class SupervisorLoginFragment extends Fragment implements OnClickListener {

    private EditText usernameEditText;
    private EditText passwordEditText;
    private String username;
    private String password;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.generic_login_fragment, container, false);
        TextView title = (TextView) v.findViewById(R.id.titleTextView);
        title.setText(R.string.supervisor_login);

        usernameEditText = (EditText) v.findViewById(R.id.usernameEditText);
        passwordEditText = (EditText) v.findViewById(R.id.passwordEditText);
        Button loginButton = (Button) v.findViewById(R.id.loginButton);
        loginButton.setOnClickListener(this);

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
                "application/hal+json",
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

        // record resource links obtained from login
        new ParseLinksTask(new ParseLinkListener()).execute(httpTaskResponse.getInputStream());

    }

    private void onConnectedButNotAuthenticated() {
        // delete unauthorized user from tablet database
        // to prevent login when not connected to network
        showLongToast(getActivity(), R.string.supervisor_bad_credentials);
        deleteSupervisor();
    }

    private void deleteSupervisor() {
        UserGateway userGateway = GatewayRegistry.getUsesrGateway();
        User user = userGateway.getFirst(getActivity().getContentResolver(), userGateway.findByUsername(username));

        if (null != user) {
            userGateway.deleteById(getActivity().getContentResolver(), user.getUuid());
        }
    }

    private void addSupervisor() {
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(password);
        user.setUuid(UUID.randomUUID().toString());
        UserGateway userGateway = GatewayRegistry.getUsesrGateway();
        userGateway.insertOrUpdate(getActivity().getContentResolver(), user);
    }

    private void onNotConnected() {
        UserGateway userGateway = GatewayRegistry.getUsesrGateway();
        User user = userGateway.getFirst(getActivity().getContentResolver(),userGateway.findByUsername(username));

        if (!password.equals(user.getPasswordHash())) {
            showLongToast(getActivity(), R.string.supervisor_bad_credentials);
            return;
        }
        launchSupervisorMainActivity();
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

    private class ParseLinkListener implements ParseLinksTask.LinkHandler {
        @Override
        public void handleLinks(Map<String, Link> links) {
            ResourceLinkRegistry.addLinks(links);
            launchSupervisorMainActivity();
        }
    }
}
