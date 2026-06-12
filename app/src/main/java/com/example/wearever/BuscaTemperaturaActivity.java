package com.example.wearever;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

// Classe que gerencia a tela de busca de cidades por temperatura
public class BuscaTemperaturaActivity extends AppCompatActivity {

    // Declaração dos componentes da interface (Views)
    private EditText etTemperatura;
    private CardView cardResultado;
    private TextView tvCidadeResultado, tvDistancia, tvTemperaturaResultado, tvCondicaoResultado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Inicializa a Activity
        super.onCreate(savedInstanceState);
        // Define o layout XML que será usado (activity_busca_temperatura.xml)
        setContentView(R.layout.activity_busca_temperatura);

        // Vincula as variáveis aos componentes reais definidos no XML através do ID
        etTemperatura        = findViewById(R.id.etTemperatura);
        cardResultado        = findViewById(R.id.cardResultado);
        tvCidadeResultado    = findViewById(R.id.tvCidadeResultado);
        tvDistancia          = findViewById(R.id.tvDistancia);
        tvTemperaturaResultado = findViewById(R.id.tvTemperaturaResultado);
        tvCondicaoResultado  = findViewById(R.id.tvCondicaoResultado);

        // Configura o botão de voltar para encerrar a atividade atual e retornar à anterior
        findViewById(R.id.ivVoltar).setOnClickListener(v -> finish());

        // Configura o clique do botão "Buscar" para disparar o método realizarBusca()
        Button btnBuscar = findViewById(R.id.btnBuscar);
        btnBuscar.setOnClickListener(v -> realizarBusca());
    }

    // Método que executa a lógica de busca quando o botão é clicado
    private void realizarBusca() {
        // Obtém o texto digitado, remove espaços em branco extras
        String input = etTemperatura.getText().toString().trim();

        // Validação básica: verifica se o campo está vazio
        if (input.isEmpty()) {
            Toast.makeText(this, "Digite uma temperatura", Toast.LENGTH_SHORT).show();
            return;
        }

        // Converte o texto para um número inteiro
        int temperatura = Integer.parseInt(input);

        // Lógica de Mock (Simulação): Define valores fixos para demonstrar o funcionamento da interface
        // Na Entrega 3, isso será substituído pela consulta real ao banco de dados ou API
        tvCidadeResultado.setText("Campos do Jordão, SP");
        tvDistancia.setText("182 km");
        tvTemperaturaResultado.setText(temperatura + "°C");
        tvCondicaoResultado.setText("Neve");

        // Torna o card de resultado visível para o usuário após a "busca"
        cardResultado.setVisibility(View.VISIBLE);
    }
}
