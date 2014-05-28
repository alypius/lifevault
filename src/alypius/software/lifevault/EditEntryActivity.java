package alypius.software.lifevault;

import java.io.File;
import java.util.*;

import android.app.*;
import android.content.*;
import android.content.DialogInterface.OnDismissListener;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.*;
import android.hardware.*;
import android.location.*;
import android.media.*;
import android.net.Uri;
import android.os.*;
import android.provider.*;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.view.*;
import android.view.View.OnCreateContextMenuListener;
import android.widget.*;

import alypius.software.lifevault.interfaces.*;
import alypius.software.lifevault.factories.*;
import alypius.software.lifevault.controllers.*;
import alypius.software.lifevault.models.DBRow;

public class EditEntryActivity extends Activity implements OnCreateContextMenuListener, OnDismissListener, LocationListener, OnInitListener, SensorEventListener {
    //Activity identifiers
    private final int ACTIVITY_IMAGE_GALLERY = 11;
    private final int ACTIVITY_AUDIO_GALLERY = 12;
    private final int ACTIVITY_VIDEO_GALLERY = 13;
    private final int ACTIVITY_CAMERA = 21;
    private final int ACTIVITY_AUDIO_RECORDING = 22;
    private final int ACTIVITY_VIDEO_RECORDING = 23;
    private final int ACTIVITY_CONTACT = 40;
    private final int IMAGE_DIALOG = 31;
    private final int AUDIO_DIALOG = 32;
    private final int VIDEO_DIALOG = 33;
    private final int DESCR_DIALOG = 41;
    private final int TITLE_DIALOG = 42;

    //Graphics identifiers
    private final int AUDIO_GRAPHIC_RESOURCE = R.drawable.boombox_reggae;
    private final int CONTACT_GRAPHIC_RESOURCE = R.drawable.contacts_icon;
    private final int MAIN_IMAGE_IMAGEVIEW = R.id.item_picture;

    private DialogManager dialogManager;
    private DialogViewManager dialogViewManager;
    private ViewRowManager viewRowManager;
    private IDBRow dbRow;

    private ImageView imageScreen;
    private MediaPlayer audioPlayer;
    private VideoView videoScreen;

    private String tempMediaIdForContextMenu;

    //Location
    private LocationManager locationManager;
    private Location currentLocation;
    //TTS
    private TextToSpeech mySpeaker;
    private boolean textToSpeechEnabled;
    //Orientation
    private boolean orientationAvailable;


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.entry_layout);

        initialize();

        //Get row id in the database of this entry from list activity
        Bundle bundle = getIntent().getExtras();
        int entryId = bundle.getInt("id");

        dbRow = new DBRow(new DataSQLiteDB(this), entryId);
        dbRow.retrieveFromDB();

        updateUI();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mySpeaker.shutdown();
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private void updateUI() {
        fillDate();
        fillTitle();
        fillDescription();
        fillLocation();
        fillOrientation();

        fillImageImageViews();
        fillAudioImageViews();
        fillVideoImageViews();
        fillContactImageViews();
    }

    private void initializeImageContextMenuListeners(IViewRow viewRow) {
        for(IViewRowElement e: viewRow.getViewRowElements()) {
            findViewById(e.getImageViewId()).setOnCreateContextMenuListener(this);
        }
    }

    private void initialize() {

        //Run when this activity is started
        imageScreen = null;
        audioPlayer = null;
        videoScreen = null;
        tempMediaIdForContextMenu = "";

        dialogManager = new DialogManager();
        dialogViewManager = new DialogViewManager();

        viewRowManager = new ViewRowManager.Builder()
                .setImageViewRow(
                        new int[]{R.id.imageview_image1, R.id.imageview_image2, R.id.imageview_image3},
                        new int[]{R.id.textview_image1, R.id.textview_image2, R.id.textview_image3},
                        new SetImageRowCell()
                )
                .setAudioViewRow(
                        new int[] { R.id.imageview_audio1, R.id.imageview_audio2, R.id.imageview_audio3 },
                        new int[] { R.id.textview_audio1, R.id.textview_audio2, R.id.textview_audio3 },
                        new SetAudioRowCell()
                )
                .setVideoViewRow(
                        new int[] { R.id.imageview_video1, R.id.imageview_video2, R.id.imageview_video3 },
                        new int[] { R.id.textview_video1, R.id.textview_video2, R.id.textview_video3 },
                        new SetVideoRowCell()
                )
                .setContactViewRow(
                        new int[] { R.id.imageview_contact1, R.id.imageview_contact2, R.id.imageview_contact3 },
                        new int[] { R.id.textview_contact1, R.id.textview_contact2, R.id.textview_contact3 },
                        new SetContactRowCell()
                )
                .build();

        //Set contextmenulistener for ImageViews
        initializeImageContextMenuListeners(viewRowManager.getImageViewRow());
        initializeImageContextMenuListeners(viewRowManager.getAudioViewRow());
        initializeImageContextMenuListeners(viewRowManager.getVideoViewRow());
        initializeImageContextMenuListeners(viewRowManager.getContactViewRow());

        locationManager = null;
        currentLocation = null;
        textToSpeechEnabled = false;
        mySpeaker = new TextToSpeech(this, this);
        orientationAvailable = false;
    }

    //Start managing Orientation
    public void fillOrientation() {

        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        orientationAvailable = startSensor(sensorManager, Sensor.TYPE_ORIENTATION);
    }

    //Start a sensor and listener for Orientation
    private boolean startSensor(SensorManager sensorManager, int sensorType) {

        if (sensorManager.getSensorList(sensorType).isEmpty()) {
            return false;
        } else {
            sensorManager.registerListener(this,
                    sensorManager.getDefaultSensor(sensorType), SensorManager.SENSOR_DELAY_NORMAL);
            return true;
        }
    }

    public void onAccuracyChanged(Sensor arg0, int arg1) {

    }

    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ORIENTATION && orientationAvailable) {
            IOrientation orientationEvent = OrientationFactory.create(event.values);
            if (!dbRow.getOrientation().isEqual(orientationEvent)) {
                dbRow.setOrientation(orientationEvent);
                updateSensorDisplay(orientationEvent);
            }
        }
    }

    //Updating the Orientation Text View
    private void updateSensorDisplay(IOrientation orientationEvent) {
        String displayString = "Orientation:  \n" + orientationEvent.toFormattedString() + "\n";
        ((TextView) findViewById(R.id.orientation_text)).setText(displayString);
    }

    //Update date in UI
    public void fillDate() {
        ((TextView) findViewById(R.id.entryDate)).setText(dbRow.getDate().getEntry());
    }

    //Update title in UI
    public void fillTitle() {
        String titleString = dbRow.getTitle().getEntry();
        if (!titleString.equals("")) {
            ((TextView) findViewById(R.id.entry_title)).setText(titleString);
        } else {
            ((TextView) findViewById(R.id.entry_title)).setText("Add a title");
        }
    }

    //Update description in UI
    public void fillDescription() {
        String descriptionString = dbRow.getDescription().getEntry();
        if (!descriptionString.equals("")) {
            ((TextView) findViewById(R.id.entry_description)).setText(descriptionString);
        } else {
            ((TextView) findViewById(R.id.entry_description)).setText("Add some text");
        }
    }

    //Update location in UI
    public void fillLocation() {
        String locationString = dbRow.getLocation().getEntry();
        if (!locationString.equals("")) {
            ((TextView) findViewById(R.id.location)).setText(locationString);
        } else {
            ((TextView) findViewById(R.id.location)).setText("Calculating location...");
            getLocation();
        }
    }

    private void fillImageImageViews() {
        //Set main image (used in list thumbnail/top of app)
        setMainImage((ImageView) findViewById(MAIN_IMAGE_IMAGEVIEW), dbRow.getMainImage().getEntry());
        //Set pictures row
        viewRowManager.getImageViewRow().fillViewRow(this, dbRow.getImage().getArray());
    }

    private void fillAudioImageViews() {
        viewRowManager.getAudioViewRow().fillViewRow(this, dbRow.getAudio().getArray());
    }

    private void fillVideoImageViews() {
        viewRowManager.getVideoViewRow().fillViewRow(this, dbRow.getVideo().getArray());
    }

    private void fillContactImageViews() {
        viewRowManager.getContactViewRow().fillViewRow(this, dbRow.getContact().getArray());
    }

    private void setMainImage(ImageView mainImageImageView, String mainImageUrl) {
        if (!mainImageUrl.equals("")) {
            if (new File(mainImageUrl).exists()) {
                mainImageImageView.setImageBitmap(Helper.getImageFromFileName(mainImageUrl));
            } else {
                removeImage(mainImageUrl);
            }
        } else {
            mainImageImageView.setImageResource(R.drawable.blankpic);
        }
    }

    private class SetImageRowCell implements ISetRowCell {
        public void call(ImageView imageView, TextView textView, String imageUrl) {
            if (new File(imageUrl).exists()) {
                imageView.setImageBitmap(Helper.getImageFromFileName(imageUrl));
                imageView.setTag(imageUrl);
                String[] textArray = imageUrl.split("/");
                String[] fileName = textArray[textArray.length - 1].split("\\.");
                textView.setText(fileName[0]);
            } else {
                removeImage(imageUrl);
            }
        }
    }

    private class SetAudioRowCell implements ISetRowCell {
        public void call(ImageView imageView, TextView textView, String audioUrl) {
            if (new File(audioUrl).exists()) {
                imageView.setTag(audioUrl);
                imageView.setImageResource(AUDIO_GRAPHIC_RESOURCE);
                String[] textArray = audioUrl.split("/");
                String[] fileName = textArray[textArray.length - 1].split("\\.");
                textView.setText(fileName[0]);
            } else {
                removeAudio(audioUrl);
            }
        }
    }

    private class SetVideoRowCell implements ISetRowCell {
        public void call(ImageView imageView, TextView textView, String videoUrl) {
            if (new File(videoUrl).exists()) {
                //If using API 2.1 or below, use slow querying to find video thumbnails
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
                    int videoId = getVideoContentId(videoUrl);
                    if (videoId > -1) {
                        imageView.setTag(videoUrl);
                        imageView.setImageBitmap(MediaStore.Video.Thumbnails.getThumbnail(
                                getContentResolver(), videoId, MediaStore.Video.Thumbnails.MINI_KIND, null));
                        String[] textArray = videoUrl.split("/");
                        String[] fileName = textArray[textArray.length - 1].split("\\.");
                        textView.setText(fileName[0]);
                    } else {
                        removeVideo(videoUrl);
                    }
                    //If using API 2.2 or above, create a runtime thumbnail using API 8 method
                } else {
                    imageView.setTag(videoUrl);
                    imageView.setImageBitmap(ThumbnailUtils.createVideoThumbnail(videoUrl, MediaStore.Video.Thumbnails.MINI_KIND));
                    String[] textArray = videoUrl.split("/");
                    String[] fileName = textArray[textArray.length - 1].split("\\.");
                    textView.setText(fileName[0]);
                }
            } else {
                removeVideo(videoUrl);
            }
        }
    }

    private class SetContactRowCell implements ISetRowCell {
        public void call(ImageView imageView, TextView textView, String contactUrl) {
            imageView.setTag(contactUrl);
            imageView.setImageResource(CONTACT_GRAPHIC_RESOURCE);
            Cursor contactsCursor = managedQuery(Uri.parse(contactUrl), null, null, null, null);
            if (contactsCursor.moveToFirst()) {
                String contactName = contactsCursor.getString(contactsCursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));
                textView.setText(contactName);
            } else {
                removeContact(contactUrl);
            }
        }
    }

    //Handles ordinary buttons
    public void myClickHandler(View view) {

        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.text_back:
                finish();
                break;
            case R.id.add_image:
                Intent imageGalleryIntent = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                startActivityForResult(imageGalleryIntent, ACTIVITY_IMAGE_GALLERY);
                break;
            case R.id.add_audio:
                Intent audioGalleryIntent = new Intent(Intent.ACTION_PICK,
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(audioGalleryIntent, ACTIVITY_AUDIO_GALLERY);
                break;
            case R.id.add_video:
                Intent videoGalleryIntent = new Intent(Intent.ACTION_PICK,
                        MediaStore.Video.Media.INTERNAL_CONTENT_URI);
                startActivityForResult(videoGalleryIntent, ACTIVITY_VIDEO_GALLERY);
                break;
            case R.id.take_photo:
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, ACTIVITY_CAMERA);
                break;
            case R.id.record_audio:
                Intent audioRecordingIntent = new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
                startActivityForResult(audioRecordingIntent, ACTIVITY_AUDIO_RECORDING);
                break;
            case R.id.record_video:
                Intent videoRecordingIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                startActivityForResult(videoRecordingIntent, ACTIVITY_VIDEO_RECORDING);
                break;
            case R.id.add_contact:
                Intent contactIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                startActivityForResult(contactIntent, ACTIVITY_CONTACT);
                break;
            case R.id.previous_image:
                viewRowManager.getImageViewRow().previousCell(this, dbRow.getImage().getArray());
                break;
            case R.id.next_image:
                viewRowManager.getImageViewRow().nextCell(this, dbRow.getImage().getArray());
                break;
            case R.id.previous_audio:
                viewRowManager.getAudioViewRow().previousCell(this, dbRow.getAudio().getArray());
                break;
            case R.id.next_audio:
                viewRowManager.getAudioViewRow().nextCell(this, dbRow.getAudio().getArray());
                break;
            case R.id.previous_video:
                viewRowManager.getVideoViewRow().previousCell(this, dbRow.getVideo().getArray());
                break;
            case R.id.next_video:
                viewRowManager.getVideoViewRow().nextCell(this, dbRow.getVideo().getArray());
                break;
            case R.id.previous_contact:
                viewRowManager.getContactViewRow().previousCell(this, dbRow.getContact().getArray());
                break;
            case R.id.next_contact:
                viewRowManager.getContactViewRow().nextCell(this, dbRow.getContact().getArray());
                break;
            case R.id.tts_button:
                if (textToSpeechEnabled) {
                    if (mySpeaker.isSpeaking()) {
                        mySpeaker.stop();
                    }
                    String descriptionString = dbRow.getDescription().getEntry();
                    if (descriptionString != null && descriptionString.length() > 0) {
                        mySpeaker.speak(descriptionString, 0, null);
                    }
                }
                break;
            case R.id.edit_title:
                showDialog(TITLE_DIALOG);
                onPrepareDialog(TITLE_DIALOG, "");
                break;
            case R.id.edit_description:
                showDialog(DESCR_DIALOG);
                onPrepareDialog(DESCR_DIALOG, "");
                break;
            default:
                break;
        }
    }

    private void displayDialog(View view, int dialogId) {

        if (!view.getTag().equals("")) {
            String resourceLocation = view.getTag().toString();
            showDialog(dialogId);
            onPrepareDialog(dialogId, resourceLocation);
        }
    }

    //Handles clicks on imageviews
    public void displayClickHandler(View view) {

        switch (((View) view.getParent()).getId()) {
            case R.id.imagegroup:
                displayDialog(view, IMAGE_DIALOG);
                break;
            case R.id.audiogroup:
                displayDialog(view, AUDIO_DIALOG);
                break;
            case R.id.videogroup:
                displayDialog(view, VIDEO_DIALOG);
                break;
            default:
                break;
        }
    }


    //Handles buttons inside dialogs
    public void dialogClickHandler(View view) {

        switch (view.getId()) {
            case R.id.image_dialog_close:
                dialogManager.getImageDialog().dismiss();
                break;
            case R.id.audio_dialog_close:
                dialogManager.getAudioDialog().dismiss();
                break;
            case R.id.video_dialog_close:
                dialogManager.getVideoDialog().dismiss();
                break;
            case R.id.dismiss_title:
                dialogManager.getTitleDialog().dismiss();
                break;
            case R.id.dismiss_description:
                dialogManager.getDescriptionDialog().dismiss();
                break;
            case R.id.save_description:
                String descriptionString = ((EditText) dialogViewManager.getDescriptionDialogView()
                        .findViewById(R.id.description_dialog_text)).getText() + "";
                dbRow.getDescription().setEntry(descriptionString);
                dbRow.saveToDB();
                fillDescription();
                dialogManager.getDescriptionDialog().dismiss();
                break;
            case R.id.save_title:
                String titleString = ((EditText) dialogViewManager.getTitleDialogView()
                        .findViewById(R.id.title_dialog_text)).getText() + "";
                dbRow.getTitle().setEntry(titleString);
                dbRow.saveToDB();
                fillTitle();
                dialogManager.getTitleDialog().dismiss();
                break;
            default:
                break;
        }
    }


    //Prepare dialogs
    protected void onPrepareDialog(int dialogId, String mediaLocation) {

        switch (dialogId) {
            case IMAGE_DIALOG:
                imageScreen = (ImageView) dialogViewManager.getImageDialogView()
                        .findViewById(R.id.image_dialog_image);
                imageScreen.setImageBitmap(BitmapFactory.decodeFile(mediaLocation));
                break;
            case AUDIO_DIALOG:
                ImageView audioDialogImageView = (ImageView) dialogViewManager.getAudioDialogView()
                        .findViewById(R.id.audio_dialog_image);
                audioDialogImageView.setImageResource(AUDIO_GRAPHIC_RESOURCE);
                if (mySpeaker.isSpeaking()) {
                    mySpeaker.stop();
                }
                audioPlayer = new MediaPlayer();
                audioPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                try {
                    audioPlayer.setDataSource(mediaLocation);
                    audioPlayer.prepare();
                    audioPlayer.start();
                } catch (Exception e) {
                }
                break;
            case VIDEO_DIALOG:
                videoScreen = (VideoView) dialogViewManager.getVideoDialogView()
                        .findViewById(R.id.video_dialog_video);
                videoScreen.setVideoPath(mediaLocation);
                videoScreen.start();
                break;
            case TITLE_DIALOG:
                ((TextView) dialogViewManager.getTitleDialogView()
                        .findViewById(R.id.title_dialog_text)).setText(dbRow.getTitle().getEntry());
                break;
            case DESCR_DIALOG:
                ((TextView) dialogViewManager.getDescriptionDialogView()
                        .findViewById(R.id.description_dialog_text)).setText(dbRow.getDescription().getEntry());
                break;
            default:
                break;
        }
    }

    private View createDialogView(int dialogId, int dialogRoot) {

        LayoutInflater dialogInflator = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        return dialogInflator.inflate(dialogId, (ViewGroup) findViewById(dialogRoot));
    }

    private Dialog createDialog(View dialogView) {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setView(dialogView);
        Dialog dialog = dialogBuilder.create();
        dialog.setOnDismissListener(this);
        return dialog;
    }

    //Create dialogs
    protected Dialog onCreateDialog(int dialogId) {

        switch (dialogId) {
            case IMAGE_DIALOG:
                dialogViewManager.setImageDialogView(
                        createDialogView(R.layout.image_dialog, R.id.image_dialog_root));
                dialogManager.setImageDialog(createDialog(dialogViewManager.getImageDialogView()));
                return dialogManager.getImageDialog();
            case AUDIO_DIALOG:
                dialogViewManager.setAudioDialogView(
                        createDialogView(R.layout.audio_dialog, R.id.audio_dialog_root));
                dialogManager.setAudioDialog(createDialog(dialogViewManager.getAudioDialogView()));
                return dialogManager.getAudioDialog();
            case VIDEO_DIALOG:
                dialogViewManager.setVideoDialogView(
                        createDialogView(R.layout.video_dialog, R.id.video_dialog_root));
                dialogManager.setVideoDialog(createDialog(dialogViewManager.getVideoDialogView()));
                return dialogManager.getVideoDialog();
            case TITLE_DIALOG:
                dialogViewManager.setTitleDialogView(
                        createDialogView(R.layout.title_dialog_layout, R.id.title_dialog));
                dialogManager.setTitleDialog(createDialog(dialogViewManager.getTitleDialogView()));
                return dialogManager.getTitleDialog();
            case DESCR_DIALOG:
                dialogViewManager.setDescriptionDialogView(
                        createDialogView(R.layout.description_dialog_layout, R.id.description_dialog));
                dialogManager.setDescriptionDialog(createDialog(dialogViewManager.getDescriptionDialogView()));
                return dialogManager.getDescriptionDialog();
            default:
                return null;
        }
    }

    //On dialog dismiss
    public void onDismiss(DialogInterface dialogInterface) {

        if (imageScreen != null) {
            imageScreen.setImageBitmap(null);
        }
        if (audioPlayer != null && audioPlayer.isPlaying()) {
            audioPlayer.stop();
            audioPlayer.release();
        }
        if (videoScreen != null && videoScreen.isPlaying()) {
            videoScreen.stopPlayback();
        }
    }

    //Launch appropriate method after activity result received
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case ACTIVITY_IMAGE_GALLERY:
                if (resultCode == Activity.RESULT_OK) {
                    updateImageFilePath(data.getData().toString());
                }
                break;
            case ACTIVITY_AUDIO_GALLERY:
                if (resultCode == Activity.RESULT_OK) {
                    updateAudioFilePath(data.getData().toString());
                }
                break;
            case ACTIVITY_VIDEO_GALLERY:
                if (resultCode == Activity.RESULT_OK) {
                    updateVideoFilePath(data.getData().toString());
                }
                break;
            case ACTIVITY_CAMERA:
                if (resultCode == Activity.RESULT_OK) {
                    updateImageFilePath(data.getData().toString());
                }
                break;
            case ACTIVITY_AUDIO_RECORDING:
                if (resultCode == Activity.RESULT_OK) {
                    updateAudioFilePath(data.getData().toString());
                }
                break;
            case ACTIVITY_VIDEO_RECORDING:
                if (resultCode == Activity.RESULT_OK) {
                    updateVideoFilePath(data.getData().toString());
                }
                break;
            case ACTIVITY_CONTACT:
                if (resultCode == Activity.RESULT_OK) {
                    addContact(data.getData().toString());
                }
                break;
            default:
                break;
        }
    }



    private String uriToFilePath(String dataType, String uri) {

        Cursor cursor = managedQuery(Uri.parse(uri), new String[]{dataType}, null, null, null);
        int columnIndex = cursor.getColumnIndexOrThrow(dataType);
        cursor.moveToFirst();
        return cursor.getString(columnIndex);
    }

    private void updateImageFilePath(String imageId) {

        String imageFilePath = uriToFilePath(MediaStore.Images.Media.DATA, imageId);
        dbRow.getImage().appendArrayElement(imageFilePath);

        //If no main image yet, set most recent image as main image
        if (dbRow.getMainImage().equals("")) {
            String[] imageUrls = dbRow.getImage().getArray();
            dbRow.getMainImage().setEntry(imageUrls[imageUrls.length - 1]);
        }

        dbRow.saveToDB();

        viewRowManager.getImageViewRow().setCalculatedDisplayIndex(dbRow.getImage().getArray());
        fillImageImageViews();
    }


    private void updateAudioFilePath(String audioId) {

        String audioFilePath = uriToFilePath(MediaStore.Audio.Media.DATA, audioId);
        dbRow.getAudio().appendArrayElement(audioFilePath);
        dbRow.saveToDB();

        viewRowManager.getAudioViewRow().setCalculatedDisplayIndex(dbRow.getAudio().getArray());
        fillAudioImageViews();
    }


    private void updateVideoFilePath(String videoId) {

        String videoFilePath = uriToFilePath(MediaStore.Video.Media.DATA, videoId);
        dbRow.getVideo().appendArrayElement(videoFilePath);
        dbRow.saveToDB();

        viewRowManager.getVideoViewRow().setCalculatedDisplayIndex(dbRow.getVideo().getArray());
        fillVideoImageViews();
    }


    private void addContact(String contactUriString) {

        dbRow.getContact().appendArrayElement(contactUriString);
        dbRow.saveToDB();

        viewRowManager.getContactViewRow().setCalculatedDisplayIndex(dbRow.getContact().getArray());
        fillContactImageViews();
    }


    //If using API 7 or below, use this to get content Uri of a video from the filepath
    private int getVideoContentId(String videoFilePath) {

        String[] videoProj = {MediaStore.Video.Media._ID, MediaStore.Video.Media.DATA};
        Cursor videoCursor = managedQuery(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, videoProj, null, null, null);
        int dataColumnIndex = videoCursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
        int idColumnIndex = videoCursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID);
        int videoId = -1;
        videoCursor.moveToFirst();
        while (videoId < 0 && videoCursor.moveToNext()) {
            String dataString = videoCursor.getString(dataColumnIndex);
            if (dataString.equals(videoFilePath)) {
                videoId = videoCursor.getInt(idColumnIndex);
            }
        }
        return videoId;
    }

    private void setMenuItem(ContextMenu menu, View view, int menuId) {

        if (!view.getTag().equals("")) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.image_menu, menu);
            menu.setHeaderTitle("Options");
            tempMediaIdForContextMenu = view.getTag().toString();
        }
    }

    //Context menu with 'delete' option for all, also 'set as main image' option for images
    public void onCreateContextMenu(ContextMenu menu, View view,
                                    ContextMenu.ContextMenuInfo menuInfo) {

        switch (((View) view.getParent()).getId()) {
            case R.id.imagegroup:
                setMenuItem(menu, view, R.menu.image_menu);
                break;
            case R.id.audiogroup:
                setMenuItem(menu, view, R.menu.audio_menu);
                break;
            case R.id.videogroup:
                setMenuItem(menu, view, R.menu.video_menu);
                break;
            case R.id.contactgroup:
                setMenuItem(menu, view, R.menu.contact_menu);
                break;
            default:
                break;
        }
    }

    //Handles menu selections
    public boolean onContextItemSelected(MenuItem item) {

        item.setChecked(true);

        switch (item.getItemId()) {
            case R.id.image_set_main:
                dbRow.getMainImage().setEntry(tempMediaIdForContextMenu);
                tempMediaIdForContextMenu = "";
                fillImageImageViews();
                return true;
            case R.id.image_delete:
                removeImage(tempMediaIdForContextMenu);
                tempMediaIdForContextMenu = "";
                return true;
            case R.id.audio_delete:
                removeAudio(tempMediaIdForContextMenu);
                tempMediaIdForContextMenu = "";
                return true;
            case R.id.video_delete:
                removeVideo(tempMediaIdForContextMenu);
                tempMediaIdForContextMenu = "";
                return true;
            case R.id.contact_delete:
                removeContact(tempMediaIdForContextMenu);
                tempMediaIdForContextMenu = "";
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean removeEntry(IDBArrayEntry<String> dbArrayEntry, String mediaId) {

        int location = Arrays.<String>asList(dbArrayEntry.getArray()).indexOf(mediaId);
        if (location > -1) {
            dbArrayEntry.setArrayElement(location, "");
            return true;
        } else {
            return false;
        }
    }

    private void removeImage(String mediaId) {

        boolean entryRemoved = removeEntry(dbRow.getImage(), mediaId);
        if (entryRemoved) {
            if (mediaId.equals(dbRow.getMainImage())) {
                String[] imageUrls = dbRow.getImage().getArray();
                dbRow.getMainImage().setEntry(imageUrls[imageUrls.length - 1]);
            }
            dbRow.saveToDB();
            fillImageImageViews();
        }
    }


    private void removeAudio(String mediaId) {

        boolean entryRemoved = removeEntry(dbRow.getAudio(), mediaId);
        if (entryRemoved) {
            dbRow.saveToDB();
            fillAudioImageViews();
        }
    }


    private void removeVideo(String mediaId) {

        boolean entryRemoved = removeEntry(dbRow.getVideo(), mediaId);
        if (entryRemoved) {
            dbRow.saveToDB();
            fillVideoImageViews();
        }
    }


    private void removeContact(String mediaId) {

        boolean entryRemoved = removeEntry(dbRow.getContact(), mediaId);
        if (entryRemoved) {
            dbRow.saveToDB();
            fillContactImageViews();
        }
    }


    //Initialize TTS
    public void onInit(int status) {

        if (status == TextToSpeech.SUCCESS &&
                mySpeaker.isLanguageAvailable(Locale.US) ==
                        TextToSpeech.LANG_COUNTRY_AVAILABLE) {
            textToSpeechEnabled = true;
        } else {
            Toast.makeText(this, "You need to install TextToSpeech",
                    Toast.LENGTH_LONG).show();
        }
    }


    //Get location
    public void getLocation() {
        if (dbRow.getLocation().equals("")) {
            locationManager = (LocationManager) (getSystemService(LOCATION_SERVICE));
            if ((currentLocation = getLastLocation(LocationManager.GPS_PROVIDER)) != null) {
                onLocationChanged(currentLocation);
            } else if ((currentLocation = getLastLocation(LocationManager.NETWORK_PROVIDER)) != null) {
                onLocationChanged(currentLocation);
            }
        }
        List providers = locationManager == null ? null : locationManager.getAllProviders();
        if (providers != null && providers.contains(LocationManager.GPS_PROVIDER))
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, this);
        if (providers != null && providers.contains(LocationManager.NETWORK_PROVIDER))
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 0, this);
    }

    //Get last location
    public Location getLastLocation(String provider) {

        Location lastLocation;

        if ((lastLocation = locationManager.getLastKnownLocation(provider)) !=
                null && (System.currentTimeMillis() - lastLocation.getTime()) / 60 < 120) {
            return lastLocation;
        } else {
            return null;
        }
    }

    public void onLocationChanged(Location newLocation) {
        currentLocation = newLocation;
        dbRow.getLocation().setEntry(getFormattedLocationString(newLocation));

        //Save to database and display
        dbRow.saveToDB();
        fillLocation();
    }

    private String getFormattedLocationString(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        String latitudeString = String.format("%.2f %s", latitude, latitude >= 0.0 ? "N" : "S");
        String longitudeString = String.format("%.2f %s", longitude, longitude >= 0.0 ? "E" : "W");
        return latitudeString + ", " + longitudeString;
    }

    public void onProviderDisabled(String provider) {

    }

    public void onProviderEnabled(String provider) {

    }

    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

}
