package com.example.lineta.Home.modalPost;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.lineta.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.UUID;

import com.example.lineta.ViewModel.UserViewModel;
import com.example.lineta.Entity.User;

public class CreatePostBottomSheet extends BottomSheetDialogFragment {

    private ActivityResultLauncher<Intent> pickFileLauncher;
    private Uri selectedFileUri;
    private String mimeType;

    Cloudinary cloudinary;
    UserViewModel userViewModel;

    public static CreatePostBottomSheet newInstance() {
        return new CreatePostBottomSheet();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "dcktmrvyv",
                "api_key", "612279453445321",
                "api_secret", "fRNSfZ7U8x2kfEdWaQBsmZTgzfU"
        ));

        pickFileLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                        selectedFileUri = result.getData().getData();
                        if (selectedFileUri != null && getView() != null) {
                            ImageView previewImage = getView().findViewById(R.id.previewImage);
                            previewImage.setVisibility(View.VISIBLE);

                            mimeType = getMimeType(selectedFileUri);

                            if (mimeType != null && mimeType.startsWith("image/")) {
                                Glide.with(requireContext()).load(selectedFileUri).into(previewImage);
                            } else if (mimeType != null && mimeType.startsWith("video/")) {
                                try {
                                    Bitmap thumbnail = getVideoThumbnail(selectedFileUri);
                                    if (thumbnail != null) {
                                        previewImage.setImageBitmap(thumbnail);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                previewImage.setVisibility(View.GONE);
                            }
                        }
                    }
                });
    }

    @Override
    public int getTheme() {
        return R.style.FullScreenBottomSheetDialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_create_post, container, false);

        EditText edtContent = view.findViewById(R.id.edtContent);
        Button btnPost = view.findViewById(R.id.btnPost);
        ImageView btnClose = view.findViewById(R.id.btnClose);
        ImageButton btnAttach = view.findViewById(R.id.btnAttach);
        ImageView previewImage = view.findViewById(R.id.previewImage);

        btnClose.setOnClickListener(v -> dismiss());

        btnPost.setEnabled(false);
        btnPost.setBackgroundResource(R.drawable.btn_post_disabled);

        edtContent.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean hasText = !s.toString().trim().isEmpty();
                btnPost.setEnabled(hasText);
                btnPost.setBackgroundResource(hasText ?
                        R.drawable.btn_post_enabled : R.drawable.btn_post_disabled);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        btnAttach.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/*", "video/*"});
            pickFileLauncher.launch(intent);
        });

        btnPost.setOnClickListener(v -> {
            String content = edtContent.getText().toString().trim();
            if (content.isEmpty()) return;

            btnPost.setEnabled(false);
            btnPost.setText("Posting...");

            if (selectedFileUri != null) {
                new Thread(() -> {
                    try {
                        File file = createFileFromUri(selectedFileUri);
                        Map uploadResult = cloudinary.uploader().upload(file, ObjectUtils.emptyMap());

                        String mediaUrl = uploadResult.get("secure_url").toString();

                        requireActivity().runOnUiThread(() -> {
                            sendPostToFirebase(content, mediaUrl);
                            btnPost.setEnabled(true);
                            btnPost.setText("Post");
                            dismiss();
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        requireActivity().runOnUiThread(() -> {
                            btnPost.setEnabled(true);
                            btnPost.setText("Post");
                        });
                    }
                }).start();
            } else {
                sendPostToFirebase(content, null);
            }
        });

        return view;
    }

    private File createFileFromUri(Uri uri) throws Exception {
        InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
        File tempFile = File.createTempFile("upload", ".tmp", requireContext().getCacheDir());
        FileOutputStream outputStream = new FileOutputStream(tempFile);
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        outputStream.close();
        inputStream.close();
        return tempFile;
    }

    private String getMimeType(Uri uri) {
        ContentResolver cR = requireContext().getContentResolver();
        return cR.getType(uri);
    }

    private Bitmap getVideoThumbnail(Uri uri) throws IOException {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(requireContext(), uri);
            return retriever.getFrameAtTime(0);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            retriever.release();
        }
    }

    private void sendPostToFirebase(String content, String mediaUrl) {

    }
}
