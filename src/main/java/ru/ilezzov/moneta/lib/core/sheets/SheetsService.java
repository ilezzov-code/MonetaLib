package ru.ilezzov.moneta.lib.core.sheets;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.List;

public class SheetsService {
    private final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private final InputStream credentialsFile;
    private final String applicationName;

    public SheetsService(final String applicationName, final InputStream credentialsFile) {
        this.applicationName = applicationName;
        this.credentialsFile = credentialsFile;
    }

    public Sheets createService() throws IOException, GeneralSecurityException {
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

        final GoogleCredentials credentials = ServiceAccountCredentials
                .fromStream(credentialsFile)
                .createScoped(List.of(SheetsScopes.SPREADSHEETS));

        return new Sheets.Builder(httpTransport, JSON_FACTORY, new HttpCredentialsAdapter(credentials))
                .setApplicationName(this.applicationName)
                .build();
    }
}
