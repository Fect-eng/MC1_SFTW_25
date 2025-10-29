package com.devlabting.tucancha.MVC.ADMIN;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.devlabting.tucancha.R;

/**
 * Modal para registrar las formas de pago (Yape/Plin, Cuentas, CCI)
 * Se despliega al presionar "Formas de Pago" en DetallesCancha_Activity
 */
public class FormasPagoDialog extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Inflar el layout del modal
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_formas_pago, null);

        // === Referencias de UI ===
        CheckBox chkYape = view.findViewById(R.id.chkYape);
        EditText etYape = view.findViewById(R.id.etYape);

        CheckBox chkCuenta1 = view.findViewById(R.id.chkCuenta1);
        EditText etCuenta1 = view.findViewById(R.id.etCuenta1);
        EditText etCci1 = view.findViewById(R.id.etCci1);

        CheckBox chkCuenta2 = view.findViewById(R.id.chkCuenta2);
        EditText etCuenta2 = view.findViewById(R.id.etCuenta2);
        EditText etCci2 = view.findViewById(R.id.etCci2);

        Button btnGuardar = view.findViewById(R.id.btnGuardarPago);

        // === Lógica de activación de campos ===
        chkYape.setOnCheckedChangeListener((buttonView, isChecked) -> etYape.setEnabled(isChecked));

        chkCuenta1.setOnCheckedChangeListener((buttonView, isChecked) -> {
            etCuenta1.setEnabled(isChecked);
            etCci1.setEnabled(isChecked);
        });

        chkCuenta2.setOnCheckedChangeListener((buttonView, isChecked) -> {
            etCuenta2.setEnabled(isChecked);
            etCci2.setEnabled(isChecked);
        });

        // === Botón Guardar ===
        btnGuardar.setOnClickListener(v -> {
            // Ejemplo: puedes procesar los datos o guardarlos en Firebase / SQLite
            Toast.makeText(getContext(), "💾 Datos de pago guardados correctamente", Toast.LENGTH_SHORT).show();
            dismiss();
        });

        // === Crear y retornar el diálogo ===
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.CustomDialogTheme);
        builder.setView(view);
        return builder.create();
    }
}
