package com.example.calculadora;

import android.icu.text.DecimalFormat;
import android.icu.text.DecimalFormatSymbols;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiFunction;

public class MainActivity extends AppCompatActivity {

    Map<Character, BiFunction<Double, Double, Double>> operacions = Map.of(
            '+', Double::sum,
            '-', (Double n1, Double n2) -> n1 - n2,
            'x', (Double n1, Double n2) -> n1 * n2,
            '÷', (Double n1, Double n2) -> n1 / n2
    );

    TextView screen;
    String num = "0";
    double acumulado = 0;
    Character operadorPendent = null;
    Character ultimoOperador = null;
    double ultimoOperando = 0;
    double memoria = 0;
    boolean numeroNuevo = true;
    boolean operadorPulsado = false;
    boolean error = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        screen = findViewById(R.id.screen);

        View.OnClickListener btnNum = v -> {

            int id = v.getId();
            Button btnPulsado = (Button) v;
            char c = btnPulsado.getText().toString().charAt(0);
            
            if (error) {
                reiniciar();
            }

            if (Character.isDigit(c) || c == ',') {
                if (numeroNuevo) {
                    ultimoOperador = null;
                }
                operadorPulsado = false;

                if (c == ',') {
                    if (numeroNuevo) {
                        num = "0,";
                        numeroNuevo = false;
                    } else if (!num.contains(",")) {
                        num += ",";
                    }
                } else {
                    if (numeroNuevo || num.equals("0")) {
                        num = String.valueOf(c);
                        numeroNuevo = false;
                    } else if (num.length() < 16) {
                        num += c;
                    }
                }

                screen.setText(num);
                return;
            }

            if (id == R.id.c) {
                reiniciar();
                screen.setText(num);
                return;
            }

            // Memoria
            if (id == R.id.mc) {
                memoria = 0;
                return;

            } else if (id == R.id.mr) {
                num = formatear(memoria);
                screen.setText(num);
                numeroNuevo = true;
                operadorPulsado = false;
                return;

            } else if (id == R.id.mplus) {
                memoria += valor();
                numeroNuevo = true;
                return;

            } else if (id == R.id.mmin) {
                memoria -= valor();
                numeroNuevo = true;
                return;
            }

            if (id == R.id.eq) {
                double resultado;

                if (operadorPendent != null) {
                    double n2 = valor();
                    Double r = calcular(acumulado, operadorPendent, n2);
                    if (r == null) {
                        mostrarError();
                        return;
                    }
                    resultado = r;
                    ultimoOperador = operadorPendent;
                    ultimoOperando = n2;
                    operadorPendent = null;

                } else if (ultimoOperador != null) {
                    Double r = calcular(valor(), ultimoOperador, ultimoOperando);
                    if (r == null) {
                        mostrarError();
                        return;
                    }
                    resultado = r;

                } else {
                    return;
                }

                num = formatear(resultado);
                screen.setText(num);
                numeroNuevo = true;
                operadorPulsado = false;
                return;
            }

            Character op = operadorDe(id);
            if (op != null) {
                if (operadorPendent != null && !operadorPulsado) {
                    Double r = calcular(acumulado, operadorPendent, valor());
                    if (r == null) {
                        mostrarError();
                        return;
                    }
                    acumulado = r;
                    num = formatear(acumulado);
                    screen.setText(num);

                } else if (operadorPendent == null) {
                    acumulado = valor();
                }

                operadorPendent = op;
                operadorPulsado = true;
                numeroNuevo = true;
                ultimoOperador = null;
            }
        };

        findViewById(R.id.btn0).setOnClickListener(btnNum);
        findViewById(R.id.btn1).setOnClickListener(btnNum);
        findViewById(R.id.btn2).setOnClickListener(btnNum);
        findViewById(R.id.btn3).setOnClickListener(btnNum);
        findViewById(R.id.btn4).setOnClickListener(btnNum);
        findViewById(R.id.btn5).setOnClickListener(btnNum);
        findViewById(R.id.btn6).setOnClickListener(btnNum);
        findViewById(R.id.btn7).setOnClickListener(btnNum);
        findViewById(R.id.btn8).setOnClickListener(btnNum);
        findViewById(R.id.btn9).setOnClickListener(btnNum);
        findViewById(R.id.coma).setOnClickListener(btnNum);

        findViewById(R.id.plus).setOnClickListener(btnNum);
        findViewById(R.id.min).setOnClickListener(btnNum);
        findViewById(R.id.mul).setOnClickListener(btnNum);
        findViewById(R.id.div).setOnClickListener(btnNum);

        findViewById(R.id.eq).setOnClickListener(btnNum);

        findViewById(R.id.mr).setOnClickListener(btnNum);
        findViewById(R.id.mc).setOnClickListener(btnNum);
        findViewById(R.id.mmin).setOnClickListener(btnNum);
        findViewById(R.id.mplus).setOnClickListener(btnNum);

        findViewById(R.id.c).setOnClickListener(btnNum);
    }

    private Character operadorDe(int id) {
        if (id == R.id.plus) return '+';
        if (id == R.id.min) return '-';
        if (id == R.id.mul) return 'x';
        if (id == R.id.div) return '÷';
        return null;
    }
    private Double calcular(double n1, char op, double n2) {
        if (op == '÷' && n2 == 0) {
            return null;
        }
        double r = operacions.get(op).apply(n1, n2);
        if (Double.isInfinite(r) || Double.isNaN(r)) {
            return null;
        }
        return r;
    }
    private double valor() {
        return Double.parseDouble(num.replace(',', '.'));
    }

    private String formatear(double d) {
        DecimalFormat df = new DecimalFormat("0.##########");
        df.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.US));
        return df.format(d);
    }

    private void reiniciar() {
        num = "0";
        acumulado = 0;
        operadorPendent = null;
        ultimoOperador = null;
        ultimoOperando = 0;
        numeroNuevo = true;
        operadorPulsado = false;
        error = false;
    }

    private void mostrarError() {
        reiniciar();
        error = true;
        screen.setText("Error");
    }
}