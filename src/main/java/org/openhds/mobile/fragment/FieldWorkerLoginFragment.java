package org.openhds.mobile.fragment;

import static org.openhds.mobile.utilities.MessageUtils.showLongToast;

import org.openhds.mobile.R;
import org.openhds.mobile.activity.CensusActivity;
import org.openhds.mobile.activity.Skeletor;
import org.openhds.mobile.database.queries.Converter;
import org.openhds.mobile.database.queries.Queries;
import org.openhds.mobile.model.FieldWorker;
import org.openhds.mobile.projectdata.ProjectActivityBuilder;

import android.app.Fragment;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class FieldWorkerLoginFragment extends Fragment implements
		OnClickListener {

	public static final String FIELD_WORKER_EXTRA = "FieldWorker";

	private EditText usernameEditText;
	private EditText passwordEditText;
	private Button loginButton;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.generic_login_fragment, container,
				false);
		TextView title = (TextView) v.findViewById(R.id.titleTextView);
		title.setText(R.string.fieldworker_login);

		usernameEditText = (EditText) v.findViewById(R.id.usernameEditText);
		passwordEditText = (EditText) v.findViewById(R.id.passwordEditText);
		loginButton = (Button) v.findViewById(R.id.loginButton);
		loginButton.setOnClickListener(this);
		return v;
	}

	public void onClick(View view) {
		authenticateFieldWorker();
	}

	private String getUsernameFromEditText() {
		String username = usernameEditText.getText().toString();
		return username;
	}

	private String getPasswordFromEditText() {
		String password = passwordEditText.getText().toString();
		return password;
	}

	private void authenticateFieldWorker() {

		// current implementation does not require password
		String password = "";
		String username = getUsernameFromEditText();

		if (Queries.hasFieldWorker(getActivity().getContentResolver(),
				username, password)) {
			Cursor cursor = Queries.getFieldWorkByExtId(getActivity()
					.getContentResolver(), username);
			cursor.moveToFirst();
			FieldWorker fieldWorker = Converter.toFieldWorker(cursor, true);
			launchCensusActivity(fieldWorker);
		} else {
			showLongToast(getActivity(), R.string.field_worker_bad_credentials);
		}
	}

	private void launchCensusActivity(FieldWorker fieldWorker) {

		Intent intent = new Intent(getActivity(), Skeletor.class);

		// TODO: get a new instance of CensusActivityBuilder and put it into the
		// intent and send'er'over to skeletor

		intent.putExtra(FIELD_WORKER_EXTRA, fieldWorker);
		intent.putExtra(ProjectActivityBuilder.ACTIVITY_MODULE_EXTRA,
				ProjectActivityBuilder.getCensusActivityBuilder());

		startActivity(intent);
	}

}
