package com.example.simplechatclient;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.simplechatclient.databinding.ActivityMainBinding;

import java.beans.PropertyChangeEvent;

public class MainActivity extends AppCompatActivity implements AbstractView {

    public static final String TAG = "MainActivity";

    private ActivityMainBinding binding;

    private DefaultController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        /* Create Controller and Models */

        controller = new DefaultController();
        ExampleWebServiceModel model = new ExampleWebServiceModel();

        /* Register Activity View and Model with Controller */

        controller.addView(this);
        controller.addModel(model);

        /* Initialize Model to Default Values */

        model.initDefault();

        binding.postBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = binding.editTextMessage.getText().toString();

                controller.sendPostRequest(message);
            }
        });

    }

    @Override
    public void modelPropertyChange(final PropertyChangeEvent evt) {

        String propertyName = evt.getPropertyName();
        String propertyValue = evt.getNewValue().toString();

        Log.i(TAG, "New " + propertyName + " Value from Model: " + propertyValue);

        if ( propertyName.equals(DefaultController.ELEMENT_OUTPUT_PROPERTY) ) {

            String oldPropertyValue = binding.messageBoard.getText().toString();

            if ( !oldPropertyValue.equals(propertyValue) ) {
                binding.messageBoard.setText(propertyValue);
            }

        }

    }

}