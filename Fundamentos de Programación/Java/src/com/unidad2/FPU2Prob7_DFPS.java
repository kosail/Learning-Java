package unidad2;

// Leer un número entero de dos dígitos y determinar si es primo y además si es negativo.

import javax.swing.JOptionPane;

public class FPU2Prob7_DFPS {
    public static void main(String[] args) {
        int userNum = 0;

        do {
            userNum = Integer.parseInt(JOptionPane.showInputDialog("Ingresa un número de dos dígitos:"));
        } while(Math.abs(userNum)>99 || Math.abs(userNum)<10);


        if (userNum%2!=0 && userNum<0) {
                JOptionPane.showMessageDialog(null,"El número es primo y es negativo.");
            } else {
                JOptionPane.showMessageDialog(null,"El número no cumple con las condiciones de ser primo y negativo.");
        }

    }
}
