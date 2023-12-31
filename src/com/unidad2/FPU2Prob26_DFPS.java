package unidad2;
// El director de una escuela está organizando un viaje de estudios, y requiere determinar cuánto debe cobrar a cada alumno y cuánto debe pagar a la compañía de viajes por el servicio. La forma de cobrar es la siguiente: si son 100 alumnos o más, el costo por cada alumno es de $50.00; de 50 a 99 alumnos, el costo es de $70.00, de 30 a 49, de $90.00, y si son menos de 30, el costo de la renta del autobús es de $4000.00, sin importar el número de alumnos.

import javax.swing.JOptionPane;

public class FPU2Prob26_DFPS {
    public static void main(String[] args) {
        int numberOfStudents=0;
        float travelAgencyPayment=0, ratePerStudent=0;

        numberOfStudents = Integer.parseInt(JOptionPane.showInputDialog("Ingresa el número de estudiantes que irán al viaje:"));

        if (numberOfStudents>=100) {
            ratePerStudent = 50;
        } else if (numberOfStudents>=50 && numberOfStudents<=99) {
            ratePerStudent = 70;
        } else if (numberOfStudents>=30 && numberOfStudents<=49) {
            ratePerStudent = 90;
        } else if (numberOfStudents>=1 && numberOfStudents<=29) {
            ratePerStudent = 4000/numberOfStudents;
        } else {
            JOptionPane.showMessageDialog(null,"No ingresaste una cantidad válida de estudiantes.");
            System.exit(1);
        }

        if (numberOfStudents>=1 && numberOfStudents<=29) {
            travelAgencyPayment = 4000;
        } else {
            travelAgencyPayment = numberOfStudents * ratePerStudent;
        }

        String outputResultsMessage = String.format("El total de cobrar a los estudiantes es de %.2f MXN.\n El total de pagar a la agencia es de %.2f MXN.", ratePerStudent, travelAgencyPayment);
        JOptionPane.showMessageDialog(null,outputResultsMessage);
    }
}
