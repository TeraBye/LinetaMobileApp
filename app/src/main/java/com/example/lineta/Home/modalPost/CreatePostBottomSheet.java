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
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.lineta.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.io.IOException;
import java.util.Objects;

public class CreatePostBottomSheet extends BottomSheetDialogFragment {

    private ActivityResultLauncher<Intent> pickFileLauncher;
    private Uri selectedFileUri;

    public static CreatePostBottomSheet newInstance() {
        return new CreatePostBottomSheet();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        pickFileLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                        selectedFileUri = result.getData().getData();
                        if (selectedFileUri != null && getView() != null) {
                            ImageView previewImage = getView().findViewById(R.id.previewImage);
                            previewImage.setVisibility(View.VISIBLE);

                            String mimeType = getMimeType(selectedFileUri);

                            if (mimeType != null && mimeType.startsWith("image/")) {
                                // Load ảnh bằng Glide
                                Glide.with(requireContext()).load(selectedFileUri).into(previewImage);
                            } else if (mimeType != null && mimeType.startsWith("video/")) {
                                // Lấy thumbnail video
                                Bitmap thumbnail = null;
                                try {
                                    thumbnail = getVideoThumbnail(selectedFileUri);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                                if (thumbnail != null) {
                                    previewImage.setImageBitmap(thumbnail);
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
            if (!content.isEmpty()) {
                // TODO: Gửi content + file lên server
                dismiss();
            }
        });

        return view;
    }

    private String getMimeType(Uri uri) {
        ContentResolver cR = requireContext().getContentResolver();
        return cR.getType(uri);
    }

    private Bitmap getVideoThumbnail(Uri uri) throws IOException {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(requireContext(), uri);
            return retriever.getFrameAtTime(0); // lấy frame đầu
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            retriever.release();
        }
    }
}
