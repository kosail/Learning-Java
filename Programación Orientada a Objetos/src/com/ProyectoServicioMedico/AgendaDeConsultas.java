/*
 * Program description: A simple medical appointments CLI system
 * Version: 2024.0.2
 * Minimum Java version required: 21.0
 * 
 * Programmers: katsuko	(https://github.com/Ka7suk0)
 * 				kosail	(https://github.com/kosail)
 * Year: 2024
 */

package com.ProyectoServicioMedico;

// Imports to interact with the user
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

// Read and write objects to/from disk
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;

// To manage all information in memory
import java.util.List;
import java.util.ArrayList;

// For functional programming methods
import java.util.Optional;
import java.util.function.Consumer;

public class AgendaDeConsultas {
	public static void main(String[] args) {
		// Load the medics and patients information or if it fails, throw a RuntimeException to ensure the program will be halted.
		// We decided to use generics to have only one function (as it is the same exact code, with the cast type different for each method). Nevertheless if we use different methods that read the serialized objects and return that object casted to the specific need (Medico, Paciente or Consulta), Java will warn about unsafe cast thus need to use the SuppressWarnings annotation on every each method.
		// Since WE ARE already handling the possibility of a ClassCastException and ClassNotFoundException on the generic method (in case the serialized object is not an ArrayList<?>), and even in the case the user inputs a serialized object that it is in fact an ArrayList<> but not of Medico, Paciente or Consulta, it will fail the cast in this main method and throw a RuntimeException making virtually impossible to run this program if no Medico, Paciente or Consulta information is provided.
		// Enough safety guards make this possible and safe to use.
		@SuppressWarnings("unchecked")
		List<Medico> medics = (ArrayList<Medico>) retrieveData("Medicos.dat").orElseThrow(() -> new RuntimeException("Fallo en carga de información de los médicos."));
		
		@SuppressWarnings("unchecked")
		List<Paciente> patients = (ArrayList<Paciente>) retrieveData("Pacientes.dat").orElseThrow(() -> new RuntimeException("Fallo en carga de información de los pacientes."));
		
		@SuppressWarnings("unchecked")
		List<Consulta> appointments = (ArrayList<Consulta>) retrieveData("Consultas.dat").orElse(new ArrayList<>(50)); // Try to load previously created appointments. If it fails, then just create an empty ArrayList and continue
		if (appointments.size() == 0) System.out.println("Se ha creado una nueva base de datos de consultas.\n");

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in)); // Needed for IO of the user

		while (true) {
			int option = 0;

			try {
				option = menu(br, option);

				if (option == 0) break;
				
				switch(option) { // Pattern matching will make things way easier for this specific case.
					case 1 -> createNewAppointment(br, medics, patients, appointments);
					case 2 -> exportAppointmentsToDisk.accept(appointments); // Note for myself: I really wanted to use pure functional interfaces, and I get that it's not good to mix programming paradigms, but welp, I'm just trying to put into practice what I'm currently learning and having fun in the process.
					case 3 -> reportAppointmentPerMedic(br, appointments, medics);
					case 4 -> reportAppointmentPerPatience(br, appointments, patients);
					case 5 -> reportAppointmentsPerDay(br, appointments);
					default -> System.err.println("Has ingresado una opción no válida. Verifica la entrada que has proveído.\n");
				}

			} catch (IOException e) {
				System.err.println("Ha ocurrido un error de entrada o salida y no es posible interactuar con el usuario.\nCerrando sistema.\n");
				System.exit(1);

			} catch (NumberFormatException e) {
				System.err.println("Has ingresado una opción no válida. Verifica la entrada que has proveído.\n");
			}
		}

		System.out.println("Gracias por usar nuestro sistema de agendado de citas.");
	} // End of main method.

	static Optional<List<?>> retrieveData(String fileName) {
		
		try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fileName))) {
			return Optional.of( (ArrayList<?>) ois.readObject() );			
		} catch (IOException e) {
			System.err.println("El archivo " + fileName + "  no pudo ser leído o no existe.");	
		} catch (ClassNotFoundException | ClassCastException e) {
			System.err.println("El archivo " + fileName + " parece estar corrupto. Asegúrate que contenga información válida.");	
		}
		
		return Optional.empty();
	}

	static int menu(BufferedReader br, int option) throws IOException, NumberFormatException {
		System.out.println("+------------------------------------+\n");
		System.out.println("Control de citas médicas (v2024.0.2)");
		System.out.println("+------------------------------------+\n");
		System.out.println("Selecciona una opción: ");
		System.out.print("\t1) Registrar citas nuevas\n\t2) Exportar todas las citas al almacenamiento.\n\t3) Reporte de citas pendientes por médico\n\t4) Historial de citas por paciente\n\t5) Busqueda de citas por día\n\n\t0) Salir\n\n>> ");
		
		return Integer.parseInt(br.readLine());
	}

	static void createNewAppointment(BufferedReader br, List<Medico> medics, List<Paciente> patients, List<Consulta> appointments) throws IOException, NumberFormatException {
		System.out.println("+------------------------------------+\n");
		System.out.println("\tAgendar una nueva cita");
		System.out.println("+------------------------------------+\n");
		
		// Getting the medic information
		System.out.print("Ingresa la cédula del médico o presiona enter para desplegar la lista de nuestros médicos disponibles.\n\n>> ");
		String input = br.readLine();
		Medico selectedMedic = findMedicByIdOrDisplayList(br, input, medics); // Logic encapsulated in it's own method to keep this one simple to the eyes.

		if (selectedMedic == null) {
        	return; // Exit the method if no valid medic selected.
		}

		System.out.println("El médico seleccionado es: " + selectedMedic.getNombre()); // Confirm with the user if they want to continue with the selected medic
		System.out.print("\n¿Desea continuar?\n1) Sí, confirmar.\n2) No, cancelar cita.\n\n>> ");
		int continueWithAppointment = Integer.parseInt(br.readLine()); // If the user inputs anything that is not a number, propagate to the main method the NumberFormatException. And if the user inputs any number that is not 1, by default cancel the appointment creation.

		if (continueWithAppointment != 1) {
			System.out.println("Cita cancelada. Regresando al menú principal.\n");
			return;
		}

		// Getting the patience information
		System.out.print("Ingresa el número de expediente del paciente: ");
		input = br.readLine();
		int patientExpedientNum = Integer.parseInt(input); // Propagates to main method if a NumberFormatException occurs...

		Paciente selectedPatience = patients.stream()
											.filter(pat -> pat.getExpediente() == patientExpedientNum)
											.findFirst()
											.orElse(null); // Return null if no matching patient found by expedient number. For security purposes, we cannot disclousure patients names as we did with the medics

		if (selectedPatience == null) {
			return; // Exit the method if no valid patience selected.
		}


		// Getting the date of the desired appointment
		System.out.print("Ingresa el número del mes de la cita deseada: ");
		int month = Math.max(1, Math.min(12, Integer.parseInt(br.readLine()))); // Using Math.max and min as safety guards to make sure we will always get values in between 1st and 12th month

		System.out.print("Ingresa el número del día de la cita deseada: ");
		// The following three lines are safety guards establising the limit of the day that the appointment can be created per month, because it makes no sense to be able to create an appointment due February 31, like woat ???
		int safetyGuardDayLimit = 31;
		if (month == 4 || month == 6 || month == 9 || month == 11) safetyGuardDayLimit = 30;
		if (month == 2) safetyGuardDayLimit = 28;

		int day = Math.max(1, Math.min(safetyGuardDayLimit, Integer.parseInt(br.readLine()) )); // It will always store a number between 1 and the last day of the selected month. Good!

		System.out.print("Ingresa la hora de la cita, en horario de 24 horas:\nEj: \t8 para las 8:00 AM\n\t15 para las 3:00 PM\n\n>> ");
		int hour = Math.max(1, Math.min(23, Integer.parseInt(br.readLine()) )); // Safety guard to ensure that hour will always be between 1 and 23. We are a 24 hours hospital ya know.

		if (appointments.add(new Consulta(selectedPatience, selectedMedic, month, day, hour)) ) {
			System.out.print("Cita agendada exitosamente. ¿Agendar una nueva cita?\n   1) Sí, agendar una nueva cita.\n   2) No, volver al menú principal.\n\n>> ");
			int doAddNewAppointment = Integer.parseInt(br.readLine());
			
			if (doAddNewAppointment == 1) createNewAppointment(br, medics, patients, appointments);
		} else {
			System.err.println("Ha ocurrido un error registando la cita.\nRegresando al menú principal.\n\n");
		}
	}

	private static Medico findMedicByIdOrDisplayList(BufferedReader br, String input, List<Medico> medics) throws IOException {
		try {
			if (input.isEmpty()) { // If the user pressed enter without any input then directly print all the medics
				System.out.println("Lista de médicos disponibles:\n");
				for (int i = 0; i < medics.size(); i++) {
					System.out.printf("\t%d) %s\n", i + 1, medics.get(i).getNombre());
				}
		
				System.out.print("\nIngresa el número de índice del médico para seleccionarlo.\n>> ");
				input = br.readLine(); // Get the index of the medic and reuse the input variable to store it.

				// Cast "input" variable to int and get the medic in that position. Then, return that medic. If it fails to do it (e.g the user inputed a number with index of medic does not exist) then return a null.
				Medico selectedMedic = null;
				try {
					selectedMedic = Optional.of( medics.get(Integer.parseInt(input) - 1) ).orElse(null);
					return selectedMedic;

				} catch (IndexOutOfBoundsException e) {
					System.err.println("El médico seleccionado no existe. Regresando al menú principal.\n");
				}

				return null;
			}
	
			// If the variable "input" was not empty, then it must contain the medic ID. If it fails, it will go directly to the catch NumberFormatException
			int numericInput = Integer.parseInt(input);

			Medico selectedMedic = medics.stream()
						.filter(med -> med.getCedula() == numericInput)
						.findFirst()
						.orElse(null); // Return null if no matching medic found by ID.

			if (selectedMedic == null) System.err.println("No se ha encontrado un médico que coincida con la solicitud. Regresando al menú principal\n");
		} catch (NumberFormatException e) {
			System.err.println("Ingresaste una opción no válida.\nNo ha sido posible seleccionar un médico. Regresando al menú principal.\n");
		}

		return null; // Return null for invalid input.
	}

	static Consumer<List<Consulta>> exportAppointmentsToDisk = (appointments) -> {
		try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("Consultas.dat"))) {
			oos.writeObject(appointments);
			System.out.println("Citas guardadas en el almacenamiento exitosamente.\n");
		} catch (IOException e) {
			System.err.println("No ha sido posible guardar un registro de las consultas debido a un error de lectura/escritura con el almacenamiento.\nImprimiendo en pantalla todas las citas:\n");

			appointments.stream().forEach((a) -> System.out.println(a.toString()));
		}
	};

	static void reportAppointmentPerMedic(BufferedReader br, List<Consulta> appointments, List<Medico> medics) throws IOException {
        System.out.println("+---------------------------------+\n");
        System.out.println("Reporte de citas pendientes por médico\n");
        System.out.println("+---------------------------------+\n");
        int op = 0;
        int index = -1;
        while (op < 1 || op > 2){
            System.out.println("Selecciona una opción: ");
            System.out.print("\t1) Ingresar número de cédula del médico\n\t2) Ingresar el nombre para buscar el médico\n\n>> ");
            op = Integer.parseInt(br.readLine());
            if (op >= 1 && op <= 2){
                switch (op) {
                    case 1:
                        System.out.print("\nNúmero de cédula:\n\n>> ");
                        int cedula = Integer.parseInt(br.readLine());
                        boolean found = false;
                        for (int i = 0; i < medics.size(); i++) {
                            if(medics.get(i).getCedula() == cedula){
                                found = true;
                                index = i;
                                break;}}
                        if (found==false){
                            System.out.println("\nEl número de cédula ingresado no coincide con ningún médico registrado.\n-----------------------------\n");
                            op = 0;}
                        continue;
                    case 2:
                        System.out.print("\nBuscar:\n\n>> ");
                        String nombre = br.readLine();
                        List<Integer> busqueda = new ArrayList<>();
                        int num=1;
                        for (int i = 0; i < medics.size(); i++) {
                            if(medics.get(i).getNombre().toLowerCase().contains(nombre.toLowerCase())){
                                System.out.println("\t" + num + ") " + medics.get(i).getNombre());
                                num++;
                                busqueda.add(i);}}
                        if (busqueda.size()>0){
                            System.out.print(">> ");
                            int input = Integer.parseInt(br.readLine());
                            if (input >0 && input <=busqueda.size()){
                                index = busqueda.get(input-1);
                            }else{
                                System.out.println("\nHas ingresado una opción no válida.\n");
                                op = 0;
                                continue;}
                        }else{
                            System.out.println("\nNo se encontró ningún médico que coincida con \""+nombre+"\"\n");
                            op = 0;}
                        continue;}
            }else{
                System.out.println("Has ingresado una opción no válida. Verifica la entrada que has proveído.\n");
            }
        }
        boolean hayCitas=false;
                for (int i = 0; i < appointments.size(); i++) {
                    if (appointments.get(i).getMedico().getCedula() == medics.get(index).getCedula()){
                        if(!hayCitas){
                            System.out.println("--------------------------------");
                            System.out.println("Fecha\tHora\tPaciente");
                            System.out.println("--------------------------------");}
                        System.out.println(appointments.get(i).getDia() + "/" + appointments.get(i).getMes() + "\t" + appointments.get(i).getHora() + "\t" + appointments.get(i).getPaciente().getNombre());
                        hayCitas=true;}}
                if(!hayCitas){
                    System.out.println("\nNo se encontraron citas agendadas para " + medics.get(index).getNombre() + "\n");
                }else{
                    System.out.println("--------------------------------\n");
                }
    }

	static void reportAppointmentPerPatience(BufferedReader br, List<Consulta> appointments, List<Paciente> patients) throws IOException {
        System.out.println("+---------------------------------+\n");
        System.out.println("Reporte de citas por paciente\n");
        System.out.println("+---------------------------------+\n");
        int op = 0;
        int index = -1;
        while (op < 1 || op > 2){
            System.out.println("Selecciona una opción: ");
            System.out.print("\t1) Ingresar número de expediente del paciente\n\t2) Ingresar el nombre para buscar el paciente\n\n>> ");
            op = Integer.parseInt(br.readLine());
            if (op >= 1 && op <= 2){
                switch (op) {
                    case 1:
                        System.out.print("\nNúmero de expediente:\n\n>> ");
                        int expediente = Integer.parseInt(br.readLine());
                        boolean found = false;
                        for (int i = 0; i < patients.size(); i++) {
                            if(patients.get(i).getExpediente() == expediente){
                                found = true;
                                index = i;
                                break;}}
                        if (found==false){
                            System.out.println("\nEl número de expediente ingresado no coincide con ningún paciente registrado.\n-----------------------------\n");
                            op = 0;}
                        continue;
                    case 2:
                        System.out.print("\nBuscar:\n\n>> ");
                        String nombre = br.readLine();
                        List<Integer> busqueda = new ArrayList<>();
                        int num=1;
                        for (int i = 0; i < patients.size(); i++) {
                            if(patients.get(i).getNombre().toLowerCase().contains(nombre.toLowerCase())){
                                System.out.println("\t" + num + ") " + patients.get(i).getNombre());
                                num++;
                                busqueda.add(i);}}
                        if (busqueda.size()>0){
                            System.out.print(">> ");
                            int input = Integer.parseInt(br.readLine());
                            if (input >0 && input <=busqueda.size()){
                                index = busqueda.get(input-1);
                            }else{
                                System.out.println("\nHas ingresado una opción no válida.\n");
                                op = 0;
                                continue;}
                        }else{
                            System.out.println("\nNo se encontró ningún paciente que coincida con \""+nombre+"\"\n");
                            op = 0;}
                        continue;}
            }else{
                System.out.println("Has ingresado una opción no válida. Verifica la entrada que has proveído.\n");
            }
        }
        boolean hayCitas=false;
                for (int i = 0; i < appointments.size(); i++) {
                    if (appointments.get(i).getPaciente().getExpediente() == patients.get(index).getExpediente()){
                        if(!hayCitas){
                            System.out.println("--------------------------------");
                            System.out.println("Fecha\tHora\tMédico");
                            System.out.println("--------------------------------");}
                        System.out.println(appointments.get(i).getDia() + "/" + appointments.get(i).getMes() + "\t" + appointments.get(i).getHora() + "\t" + appointments.get(i).getMedico().getNombre());
                        hayCitas=true;}}
                if(!hayCitas){
                    System.out.println("\nNo se encontraron citas agendadas para " + patients.get(index).getNombre() + "\n");
                }else{
                    System.out.println("--------------------------------\n");
                }
    }


	static void reportAppointmentsPerDay (BufferedReader br , List<Consulta> appointments) throws IOException, NumberFormatException {
		System.out.println("+-----------------------------------+\n");
        System.out.println("\tReporte de citas por día\n");
        System.out.println("+-----------------------------------+\n");

		// Getting the date of the desired appointment check.
		// ? Note that this code is exactly the same as in the createNewAppointment method, but since it is just executed twice it might not be necessary to be modularized as a method, since it may increase the difficulty of comprehension of the code. We will stick to the simple and old way to use again this code.

		System.out.print("Ingresa el número del mes: ");
		int month = Math.max(1, Math.min(12, Integer.parseInt(br.readLine()))); // Same as the createNewAppointment method, we're using Math.max and min as safety guards to always get a value between 1 and 12

		System.out.print("Ingresa el número del día: ");
		int safetyGuardDayLimit = 31;
		if (month == 4 || month == 6 || month == 9 || month == 11) safetyGuardDayLimit = 30;
		if (month == 2) safetyGuardDayLimit = 28;

		int day = Math.max(1, Math.min(safetyGuardDayLimit, Integer.parseInt(br.readLine()) )); // It will always store a number between 1 and the last day of the selected month. Good!

		 // Get the total amount of appointments registered for the specified date
		int totalOfAppointments = (int) appointments.stream().filter((a) -> a.getMes() == month && a.getDia() == day).count();
		if (totalOfAppointments == 0) { // If there are no appointments, return to main menu.
			System.out.printf("\nNo hay citas registradas para el mes %s día %d.\n", month, day);
			return;
		}

		System.out.println("\n+---------------------------------------+");
		System.out.printf("\nCitas registradas para el mes %s día %d:\n", month, day);
		System.out.println("+---------------------------------------+\n");
		System.out.println("Paciente\tMédico\tMes\tDia\tHora");
		appointments.stream()
					.filter((a) -> a.getMes() == month && a.getDia() == day)
					.forEach((a) -> System.out.println(a.toString()));

		System.out.printf("\nTotal de citas registradas para el día: %d\n\n", totalOfAppointments);
	} 
}