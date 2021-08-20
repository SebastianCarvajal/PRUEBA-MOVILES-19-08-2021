package com.example.reconociendotexto;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ResultActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleMap.OnMapClickListener{

    Bundle b = new Bundle();

    GoogleMap mapa;
    int tipoVista;

    private static final String URL_OBTENER_DATOS = "http://www.geognos.com/api/en/countries/info/";
    RequestQueue requestQueue;

    TextView txtPais;
    TextView txtCapital;
    TextView txtTelPrefijo;
    TextView txtCenter;
    TextView txtIso2;
    TextView txtIso3;
    TextView txtIsoFips;
    TextView txtIsoNum;

    ImageView imgViewBandera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        b = this.getIntent().getExtras();
        requestQueue = Volley.newRequestQueue(this);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        txtPais = (TextView) findViewById(R.id.txtPais);
        txtCapital = (TextView) findViewById(R.id.txtCapital);
        txtTelPrefijo = (TextView) findViewById(R.id.txtTelPref);
        txtCenter = (TextView) findViewById(R.id.txtCenter);
        txtIso2 = (TextView) findViewById(R.id.txtIso2);
        txtIso3 = (TextView) findViewById(R.id.txtIso3);
        txtIsoFips = (TextView) findViewById(R.id.txtIsoFips);
        txtIsoNum = (TextView) findViewById(R.id.txtIsoNum);
        //jsonArrayRequest_ObtenerDatos("EC");
        imgViewBandera = (ImageView) findViewById(R.id.imgViewBandera);

        String A2C = b.getString("A2C");
        jsonObj(A2C);
        Glide.with(ResultActivity.this)
                .load("http://www.geognos.com/api/en/countries/flag/"+A2C+".png")
                .into(imgViewBandera);
    }

    private void jsonObj(String A2C){
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                URL_OBTENER_DATOS + A2C + ".json",
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        String StatusMsg;
                        try {
                            StatusMsg = response.getString("StatusMsg");
                            Toast.makeText(ResultActivity.this, StatusMsg, Toast.LENGTH_SHORT).show();

                            // Obtener el resultado
                            JSONObject infoCompleta = response.getJSONObject("Results");

                            // Nombre Pais
                            String nombrePais = infoCompleta.getString("Name");
                            Toast.makeText(ResultActivity.this, nombrePais, Toast.LENGTH_SHORT).show();
                            txtPais.setText(nombrePais);

                            // Capital
                            String nombreCapital = infoCompleta.getJSONObject("Capital").getString("Name");
                            //Toast.makeText(ResultActivity.this, nombreCapital, Toast.LENGTH_SHORT).show();
                            txtCapital.setText(nombreCapital);

                            //Prefijo Telefono
                            String TelPref = infoCompleta.getString("TelPref");
                            txtTelPrefijo.setText(TelPref);

                            //Center -- Valores Para centrar el mapa
                            JSONArray center = infoCompleta.getJSONArray("GeoPt");
                            double lat = Double.parseDouble(center.get(0).toString());
                            double lon = Double.parseDouble(center.get(1).toString());
                            //Toast.makeText(ResultActivity.this, String.valueOf(lat)+","+String.valueOf(lon), Toast.LENGTH_SHORT).show();
                            posicionarMapa(lat, lon);
                            txtCenter.setText(String.valueOf(lat)+" , "+String.valueOf(lon));

                            //Country Codes
                            String iso2 = infoCompleta.getJSONObject("CountryCodes").getString("iso2");
                            String iso3 = infoCompleta.getJSONObject("CountryCodes").getString("iso3");
                            String isofips = infoCompleta.getJSONObject("CountryCodes").getString("fips");
                            int isonum = infoCompleta.getJSONObject("CountryCodes").getInt("isoN");
                            //Toast.makeText(ResultActivity.this, iso2+","+iso3+","+isofips+","+String.valueOf(isonum), Toast.LENGTH_SHORT).show();
                            txtIso2.setText(iso2);
                            txtIso3.setText(iso3);
                            txtIsoFips.setText(isofips);
                            txtIsoNum.setText(String.valueOf(isonum));

                            //Rectangle
                            LatLng[] puntos = new LatLng[4];

                            double west = Double.parseDouble(infoCompleta.getJSONObject("GeoRectangle").getString("West"));
                            double east = Double.parseDouble(infoCompleta.getJSONObject("GeoRectangle").getString("East"));
                            double north = Double.parseDouble(infoCompleta.getJSONObject("GeoRectangle").getString("North"));
                            double south = Double.parseDouble(infoCompleta.getJSONObject("GeoRectangle").getString("South"));
                            Toast.makeText(ResultActivity.this, String.valueOf(west)+","+String.valueOf(east)+","+String.valueOf(north)+","+String.valueOf(south), Toast.LENGTH_SHORT).show();
                            puntos[0] = new LatLng(north,east);
                            puntos[1] = new LatLng(south,east);
                            puntos[2] = new LatLng(south,west);
                            puntos[3] = new LatLng(north,west);

                            PolylineOptions lineas = new PolylineOptions()
                                    .add(puntos[0])
                                    .add(puntos[1])
                                    .add(puntos[2])
                                    .add(puntos[3])
                                    .add(puntos[0]);
                            lineas.width(8);
                            lineas.color(Color.RED);
                            mapa.addPolyline(lineas);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //Log.d(TAG, "Error Respuesta en JSON: " + error.getMessage());

                    }
                }
        );
        requestQueue.add(jsonObjectRequest);
    }

    private void jsonArrayRequest_ObtenerDatos(String A2C){
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                URL_OBTENER_DATOS + A2C + ".json",
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        String alpha2Code;

                        int tamanio = response.length();

                        Toast.makeText(ResultActivity.this, "El tamano es :"+tamanio, Toast.LENGTH_SHORT).show();
                        for(int i=0; i<tamanio; i++){
                            try {
                                JSONObject jsonObject = new JSONObject(response.get(i).toString());
                                alpha2Code = jsonObject.getString("StatusMsg");
                                Toast.makeText(ResultActivity.this, alpha2Code, Toast.LENGTH_SHORT).show();

                                //mRevistaList.add(new revista(1, titulo, volumen, numero, anio, cover));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(ResultActivity.this, "ha ocurrido un error", Toast.LENGTH_SHORT).show();
                    }
                }
        );
        requestQueue.add(jsonArrayRequest);

    }

    @Override
    public void onMapClick(@NonNull LatLng latLng) {

    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mapa = googleMap;
        mapa.getUiSettings().setZoomControlsEnabled(true);
        mapa.setOnMapClickListener(this);
    }

    public void CambiarTipoMapa(View v){
        mapa.setMapType(tipoVista);
        tipoVista = tipoVista<4?tipoVista+1:1;
    }

    public void posicionarMapa(double lat, double lon){
        LatLng uteq = new LatLng(lat, lon);
        CameraPosition camPos = new CameraPosition.Builder()
                .target(uteq)
                .zoom(4)
                .bearing(0)
                .build();
        CameraUpdate camUpd3 = CameraUpdateFactory.newCameraPosition(camPos);
        mapa.animateCamera(camUpd3);
    }
}