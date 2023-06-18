package com.example.v2;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.squareup.picasso.Picasso;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private EditText cityEditText;
    private Button getWeatherButton;
    private TextView weatherTextView;
    private TextView conditionTextView;
    private TextView localTimeTextView;
    private TextView windSpeedTextView;
    private TextView cloudTextView;
    private TextView rainProbabilityTextView;
    private ImageView weatherIconImageView;
    private CardView weatherCardView;

    private DatabaseReference mDatabase;

    private static final String API_KEY = "6939bd5172fc469face144929230206";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cityEditText = findViewById(R.id.cityEditText);
        getWeatherButton = findViewById(R.id.getWeatherButton);
        weatherTextView = findViewById(R.id.weatherTextView);
        conditionTextView = findViewById(R.id.conditionTextView);
        localTimeTextView = findViewById(R.id.localTimeTextView);
        windSpeedTextView = findViewById(R.id.windSpeedTextView);
        cloudTextView = findViewById(R.id.cloudTextView);
        rainProbabilityTextView = findViewById(R.id.rainProbabilityTextView);
        weatherIconImageView = findViewById(R.id.weatherIconImageView);
        weatherCardView = findViewById(R.id.weatherCardView);

        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    public void onGetWeatherButtonClick(View view) {
        String city = cityEditText.getText().toString().trim();
        if (!city.isEmpty()) {
            WeatherTask weatherTask = new WeatherTask();
            weatherTask.execute(city);
            mDatabase.child("cities").push().setValue(city);
        } else {
            displayErrorMessage("Введите город");
        }
    }

    private void displayErrorMessage(String message) {
        weatherTextView.setText(message);
        conditionTextView.setText("");
        localTimeTextView.setText("");
        windSpeedTextView.setText("");
        cloudTextView.setText("");
        rainProbabilityTextView.setText("");
        weatherIconImageView.setImageDrawable(null);
        weatherCardView.setVisibility(View.VISIBLE);
    }

    private class WeatherTask extends AsyncTask<String, Void, WeatherData> {
        @Override
        protected WeatherData doInBackground(String... params) {
            String city = params[0];
            String apiUrl = "https://api.weatherapi.com/v1/forecast.json?key=" + API_KEY + "&q=" + city + "&days=5";

            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url(apiUrl)
                    .build();

            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    JSONObject jsonResponse = new JSONObject(responseData);

                    if (jsonResponse.has("error")) {
                        String errorMessage = jsonResponse.getJSONObject("error").getString("message");
                        displayErrorMessage(errorMessage);
                        return null;
                    }

                    JSONObject location = jsonResponse.getJSONObject("location");
                    String cityName = location.getString("name");

                    JSONObject current = jsonResponse.getJSONObject("current");
                    double temperature = current.getDouble("temp_c");
                    String conditionText = current.getJSONObject("condition").getString("text");
                    String iconUrl = "http:" + current.getJSONObject("condition").getString("icon");
                    String localTime = location.getString("localtime");
                    double windSpeedKph = current.getDouble("wind_kph");
                    int cloud = current.getInt("cloud");
                    double rainProbability = current.getDouble("humidity");

                    return new WeatherData(cityName, temperature, conditionText, iconUrl, localTime, windSpeedKph, cloud, rainProbability);
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(WeatherData weatherData) {
            if (weatherData != null) {
                weatherTextView.setText("Текущая погода в городе " + weatherData.getCityName() + ":\n" +
                        "Температура: " + weatherData.getTemperature() + "°C");


                localTimeTextView.setText("Локальное время: " + weatherData.getLocalTime());
                windSpeedTextView.setText("Скорость ветра: " + weatherData.getWindSpeedKph() + " км/ч");
                cloudTextView.setText("Облачность: " + weatherData.getCloud() + "%");
                rainProbabilityTextView.setText("Вероятность дождя: " + weatherData.getRainProbability() + " %");
                conditionTextView.setText("Состояние: " + weatherData.getConditionText());
                Picasso.get().load(weatherData.getIconUrl()).into(weatherIconImageView);

                weatherCardView.setVisibility(View.VISIBLE);
            } else {
                displayErrorMessage("Ошибка при получении погоды.");
            }
        }
    }

    private static class WeatherData {
        private String cityName;
        private double temperature;
        private String conditionText;
        private String iconUrl;
        private String localTime;
        private double windSpeedKph;
        private int cloud;
        private double rainProbability;

        public WeatherData(String cityName, double temperature, String conditionText, String iconUrl, String localTime, double windSpeedKph, int cloud, double rainProbability) {
            this.cityName = cityName;
            this.temperature = temperature;
            this.conditionText = conditionText;
            this.iconUrl = iconUrl;
            this.localTime = localTime;
            this.windSpeedKph = windSpeedKph;
            this.cloud = cloud;
            this.rainProbability = rainProbability;
        }

        public String getCityName() {
            return cityName;
        }

        public double getTemperature() {
            return temperature;
        }

        public String getConditionText() {
            return conditionText;
        }

        public String getIconUrl() {
            return iconUrl;
        }

        public String getLocalTime() {
            return localTime;
        }

        public double getWindSpeedKph() {
            return windSpeedKph;
        }

        public int getCloud() {
            return cloud;
        }

        public double getRainProbability() {
            return rainProbability;
        }
    }
}


