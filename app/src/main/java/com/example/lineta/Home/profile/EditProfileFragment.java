package com.example.lineta.Home.profile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.bumptech.glide.Glide;
import com.example.lineta.R;
import com.example.lineta.ViewModel.CurrentUserViewModel;
import com.example.lineta.service.UserService;
import com.example.lineta.service.client.ApiClient;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;

public class EditProfileFragment extends Fragment {
    private static final int PICK_IMAGE_REQUEST = 1;

    private ShapeableImageView avatarImageView;
    private EditText editFullname, editBio;
    private Button btnSave, btnChangeAvatar, btnCancel;
    private Uri selectedImageUri;

    private CurrentUserViewModel currentUserViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        avatarImageView = view.findViewById(R.id.avatarImageView);
        editFullname = view.findViewById(R.id.editFullname);
        editBio = view.findViewById(R.id.editBio);
        btnSave = view.findViewById(R.id.btnSave);
        btnChangeAvatar = view.findViewById(R.id.btnChangeAvatar);
        btnCancel = view.findViewById(R.id.btnCancel);

        currentUserViewModel = new ViewModelProvider(requireActivity()).get(CurrentUserViewModel.class);
        loadCurrentUserData();

        btnChangeAvatar.setOnClickListener(v -> openFileChooser());

        btnCancel.setOnClickListener(v -> {
            // Quay về fragment trước đó
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        btnSave.setOnClickListener(v -> {
            String newFullname = editFullname.getText().toString();
            String newBio = editBio.getText().toString();

            // Lấy userId từ ViewModel (đã có sẵn)
            currentUserViewModel.getCurrentUserLiveData().observe(getViewLifecycleOwner(), user -> {
//                String userId = user.getId();  // nếu id của bạn trong model là như vậy
                String userId = FirebaseAuth.getInstance().getUid();

                Log.d("EditProfile", "Edit uid: " + userId);

                File imageFile = null;
                if (selectedImageUri != null) {
                    try {
                        imageFile = getFileFromUri(selectedImageUri, requireContext());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                btnSave.setEnabled(false);
                updateProfile(userId, newFullname, newBio, imageFile);
            });
        });


        return view;
    }

    private void loadCurrentUserData() {
        currentUserViewModel.getCurrentUserLiveData().observe(getViewLifecycleOwner(), user -> {
            editFullname.setText(user.getFullName());
            editBio.setText(user.getBio());
            if (user.getProfilePicURL() != null) {
                Glide.with(requireContext())
                        .load(user.getProfilePicURL())
                        .placeholder(R.drawable.default_avatar)
                        .into(avatarImageView);
            }
        });
    }

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), selectedImageUri);
                avatarImageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateProfile(String userId, String fullNameStr, String bioStr, File imageFile) {

        UserService userApi = ApiClient.getRetrofit().create(UserService.class);

        // Chuẩn bị RequestBody cho fullName và bio
        RequestBody fullName = RequestBody.create(okhttp3.MultipartBody.FORM, fullNameStr);
        RequestBody bio = RequestBody.create(okhttp3.MultipartBody.FORM, bioStr);

        // Chuẩn bị file (nếu có)
        MultipartBody.Part filePart;
        if (imageFile != null) {
            RequestBody requestFile = RequestBody.create(okhttp3.MediaType.parse("multipart/form-data"), imageFile);
            filePart = MultipartBody.Part.createFormData("file", imageFile.getName(), requestFile);
        } else {
            filePart = MultipartBody.Part.createFormData("file", "");
        }

        Call<ResponseBody> call = userApi.updateUser(userId, fullName, bio, filePart);
        call.enqueue(new retrofit2.Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                btnSave.setEnabled(true);  // <-- bật lại sau khi có response

                if (response.isSuccessful()) {
                    Log.d("UpdateProfile", "Cập nhật thành công");
                    Toast.makeText(getContext(), "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                    // QUAY VỀ LẠI FRAGMENT TRƯỚC:
                    requireActivity().getSupportFragmentManager().popBackStack();
                } else {
                    Log.e("UpdateProfile", "Cập nhật thất bại: " + response.code());
                    Toast.makeText(getContext(), "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("UpdateProfile", "Lỗi: " + t.getMessage());
                Toast.makeText(getContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public File getFileFromUri(Uri uri, Context context) throws IOException {
        InputStream inputStream = context.getContentResolver().openInputStream(uri);
        File tempFile = File.createTempFile("upload", ".jpg", context.getCacheDir());
        FileOutputStream out = new FileOutputStream(tempFile);
        byte[] buffer = new byte[1024];
        int read;
        while ((read = inputStream.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
        out.flush();
        out.close();
        inputStream.close();
        return tempFile;
    }

}
