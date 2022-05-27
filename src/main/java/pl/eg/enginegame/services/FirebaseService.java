package pl.eg.enginegame.services;

import java.io.InputStream;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.eg.enginegame.*;

import javax.swing.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class FirebaseService {
    private static final Logger LOGGER= LoggerFactory.getLogger(EngineGameApp.class);
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    private Firestore db = null;

    @Autowired
    EngineGameApi ega;

    public FirebaseService() throws IOException {

//        FileInputStream serviceAccount = new FileInputStream("E:\\_PRJ\\Semestr_6\\StrategyGame\\enginegame\\src\\main\\resources\\strategygame-812c9-firebase-adminsdk-exy25-51e7e83b7d.json");
//        FileInputStream serviceAccount = new FileInputStream("src/main/resources/strategygame-812c9-firebase-adminsdk-exy25-51e7e83b7d.json");

        InputStream serviceAccount = getClass().getResourceAsStream("/" + "strategygame-812c9-firebase-adminsdk-exy25-51e7e83b7d.json");
        assert serviceAccount != null;
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

        FirebaseApp.initializeApp(options);

        db = FirestoreClient.getFirestore();
    }

    public void addLog(String msg, String logInfo) throws ExecutionException, InterruptedException {
        Map<String, Object> docData = new HashMap<>();

        LOGGER.info(msg, logInfo);
        docData.put("logInfo", msg + logInfo);


        Date date = new Date();
        Timestamp timestamp = new Timestamp(date.getTime());
        docData.put("logDate", sdf.format(timestamp));

        //zapis z automatycznie generowanym kluczem głównym
        ApiFuture<WriteResult> future = db.collection("enginegame-logs").document().set(docData);

        // future.get() blocks on response
        System.out.println("Update time : " + future.get().getUpdateTime());
    }

    public void registerSessionActon(Session session, TurnAction action, int msgErrorCode) throws ExecutionException, InterruptedException, JsonProcessingException {
        Map<String, Object> docData = new HashMap<>();

        String uuid = session.getSessionUUID();
        Timestamp timestamp = new Timestamp(new Date().getTime());

        String jsonEga = ega.jsonPrepareGameInfo(session,
                msgErrorCode,
                msgErrorCode == MsgError.OK ? MsgState.OK : MsgState.IS_ERROR);

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonAction = objectMapper.writeValueAsString(action);

        docData.put("uuid", uuid);
        docData.put("timestamp", sdf.format(timestamp));
        docData.put("action", jsonAction);
        docData.put("gameInfo", jsonEga);

        //zapis z automatycznie generowanym kluczem głównym
        ApiFuture<WriteResult> future = db.collection("enginegame-sessions")
                .document()
                .set(docData);

        // future.get() blocks on response
        System.out.println("Session update time : " + future.get().getUpdateTime());
    }

}
