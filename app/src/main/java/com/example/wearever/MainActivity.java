package com.example.wearever;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.TextView;

import com.example.wearever.adapter.PrevisaoAdapter;
import com.example.wearever.model.WeatherForecast;

import java.util.ArrayList;
import java.util.List;

// Classe principal que representa a tela de clima (Activity)
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Inicializa a Activity e define o layout XML associado (activity_main)
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Chama os métodos para preencher a tela com dados de teste
        configurarDadosMock();
        configurarRecyclerView();

        findViewById(R.id.cardBuscaTemperatura).setOnClickListener(v -> {
            startActivity(new Intent(this, BuscaTemperaturaActivity.class));
        });
    }

    // Método responsável por preencher as informações da cidade atual com dados fixos (Mock)
    private void configurarDadosMock() {
        // Busca as referências dos componentes visuais do layout (TextViews) pelo ID
        TextView tvCidade = findViewById(R.id.tvCidade);
        TextView tvTemperaturaAtual = findViewById(R.id.tvTemperaturaAtual);
        TextView tvDescricaoClima = findViewById(R.id.tvDescricaoClima);
        TextView tvTempMax = findViewById(R.id.tvTempMax);
        TextView tvTempMin = findViewById(R.id.tvTempMin);
        TextView tvUmidade = findViewById(R.id.tvUmidade);
        TextView tvVento = findViewById(R.id.tvVento);
        TextView tvSensacaoTermica = findViewById(R.id.tvSensacaoTermica);
        TextView tvUltimaAtualizacao = findViewById(R.id.tvUltimaAtualizacao);

        // Define os textos que serão exibidos na interface do usuário
        tvCidade.setText("São Paulo, BR");
        tvTemperaturaAtual.setText("23°");
        tvDescricaoClima.setText("Parcialmente nublado");
        tvTempMax.setText("28°");
        tvTempMin.setText("17°");
        tvUmidade.setText("68%");
        tvVento.setText("12 km/h");
        tvSensacaoTermica.setText("21°");
        tvUltimaAtualizacao.setText("Atualizado às 14:35");
    }

    // Método que configura a lista horizontal (RecyclerView) dos próximos dias
    private void configurarRecyclerView() {
        // Cria uma lista para armazenar as previsões dos próximos dias
        List<WeatherForecast> previsoes = new ArrayList<>();

        // Adiciona dados manuais à lista para simular a previsão da semana
        previsoes.add(criarMock("Seg", "Nublado", 28, 17));
        previsoes.add(criarMock("Ter", "Chuva", 22, 15));
        previsoes.add(criarMock("Qua", "Limpo", 30, 19));
        previsoes.add(criarMock("Qui", "Nublado", 25, 16));
        previsoes.add(criarMock("Sex", "Limpo", 27, 18));

        // Busca o RecyclerView no layout
        RecyclerView rvPrevisao = findViewById(R.id.rvPrevisao);
        
        // Define que a lista será exibida na HORIZONTAL
        rvPrevisao.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        
        // Atribui o adaptador (Adapter) que fará a ponte entre os dados e o layout de cada item
        rvPrevisao.setAdapter(new PrevisaoAdapter(this, previsoes));
    }

    // Método auxiliar para criar um objeto WeatherForecast com dados simplificados para teste
    private WeatherForecast criarMock(String dia, String condicao, double max, double min) {
        WeatherForecast f = new WeatherForecast();
        f.setCityName(dia);           // Armazena temporariamente o dia da semana no campo cityName
        f.setWeatherCondition(condicao);
        f.setTempMax(max);
        f.setTempMin(min);
        return f;
    }
}
