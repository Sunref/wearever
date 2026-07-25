package com.example.wearever.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wearever.R;
import com.example.wearever.model.WeatherForecast;

import java.util.List;
import java.util.Locale;

/**
 * Adaptador para o RecyclerView de Previsão do Tempo.
 * O Adapter é o responsável por converter uma lista de objetos em itens visíveis na tela.
 */
public class PrevisaoAdapter extends RecyclerView.Adapter<PrevisaoAdapter.PrevisaoViewHolder> {

    private final Context context;
    private final List<WeatherForecast> lista;

    /**
     * Construtor do adaptador.
     * @param context Contexto da aplicação (geralmente a Activity).
     * @param lista Lista de objetos WeatherForecast que serão exibidos.
     */
    public PrevisaoAdapter(Context context, List<WeatherForecast> lista) {
        this.context = context;
        this.lista = lista;
    }

    /**
     * Método chamado quando o RecyclerView precisa criar um novo item (uma nova "caixinha").
     */
    @NonNull
    @Override
    public PrevisaoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // "Infla" o layout XML de cada item (item_previsao.xml) para transformá-lo em uma View Java
        View view = LayoutInflater.from(context).inflate(R.layout.item_previsao, parent, false);
        return new PrevisaoViewHolder(view);
    }

    /**
     * Método chamado para preencher os dados de um item específico na tela.
     * @param holder O ViewHolder que contém as referências dos componentes visuais.
     * @param position A posição do item na lista que deve ser exibido agora.
     */
    @Override
    public void onBindViewHolder(@NonNull PrevisaoViewHolder holder, int position) {
        // Pega o objeto de previsão baseado na posição
        WeatherForecast item = lista.get(position);

        // Define o dia da semana (o campo cityName está sendo usado temporariamente como o dia no mock)
        holder.tvDiaSemana.setText(item.getCityName());

        // Formata as temperaturas máxima e mínima com o símbolo de graus (ex: 28°)
        holder.tvTempMaxDia.setText(String.format(Locale.getDefault(), "%.0f°", item.getTempMax()));
        holder.tvTempMinDia.setText(String.format(Locale.getDefault(), "%.0f°", item.getTempMin()));

        // Define o ícone de acordo com a condição climática (chuva, nublado, etc)
        holder.ivIconeClimaDia.setImageResource(resolverIcone(item.getWeatherCondition()));
    }

    /**
     * Retorna a quantidade total de itens na lista para o RecyclerView.
     */
    @Override
    public int getItemCount() {
        return lista.size();
    }

    /**
     * Método auxiliar que retorna o ícone correspondente à condição climática enviada.
     * @param condicao Texto da condição (ex: "chuva", "nublado").
     * @return O ID do recurso do ícone (R.drawable...).
     */
    private int resolverIcone(String condicao) {
        if (condicao == null) return R.drawable.ic_partly_cloudy;

        switch (condicao.toLowerCase()) {
            case "chuva":       return R.drawable.ic_partly_cloudy; // Futuro: substituir por ic_rain
            case "limpo":       return R.drawable.ic_partly_cloudy; // Futuro: substituir por ic_sun
            case "neve":        return R.drawable.ic_partly_cloudy; // Futuro: substituir por ic_snow
            case "nublado":
            default:            return R.drawable.ic_partly_cloudy;
        }
    }

    /**
     * ViewHolder: Classe interna que "guarda" as referências dos componentes visuais de um item.
     * Isso melhora a performance ao evitar chamadas repetidas ao findViewById durante a rolagem.
     */
    public static class PrevisaoViewHolder extends RecyclerView.ViewHolder {

        TextView tvDiaSemana, tvTempMaxDia, tvTempMinDia;
        ImageView ivIconeClimaDia;

        public PrevisaoViewHolder(@NonNull View itemView) {
            super(itemView);
            // Faz o vínculo entre as variáveis Java e os IDs do arquivo item_previsao.xml
            tvDiaSemana     = itemView.findViewById(R.id.tvDiaSemana);
            ivIconeClimaDia = itemView.findViewById(R.id.ivIconeClimaDia);
            tvTempMaxDia    = itemView.findViewById(R.id.tvTempMaxDia);
            tvTempMinDia    = itemView.findViewById(R.id.tvTempMinDia);
        }
    }
}
