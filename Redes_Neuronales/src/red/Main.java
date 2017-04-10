package red;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

public class Main {
	// Training data
	private static final String trainingImages = "train-images-idx3-ubyte.gz";
	private static final String trainingLabels = "train-labels-idx1-ubyte.gz";
		
	// Test data
	private static final String testImages = "t10k-images-idx3-ubyte.gz";
	private static final String testLabels = "t10k-labels-idx1-ubyte.gz";
	
	private static int imagesTraining[][][];
	private static int imagesTest[][][];
	private static int labelsTraining[];
	private static int labelsTest[];
	private static MNISTDatabase mdb;
	private static float[][] entradas;
	private static int tamanioEjecucion;
	private static float errorAcumuladoTest;
	private static ArrayList<Float> pj;
	
	//Creación de las capas de neuronas:
	private static CapaNeuronas entrada = new CapaNeuronas("entrada",28*28,1);
	private static CapaNeuronas oculta = new CapaNeuronas("oculta",256,28*28);
	private static CapaNeuronas salida = new CapaNeuronas("salida",10,256);
	
	public static void guardaPesos(CapaNeuronas[] cps) throws IOException{
		FileWriter fichero = new FileWriter("pesos_backup.txt");
		
		for(int i = 0; i<cps.length; i++){
			for(int k = 0;k<cps[i].getNumeroNeuronas();k++){
				for(int j = 0; j<cps[i].getPesos()[k].length; j++){
					fichero.write(cps[i].getPesos()[k][j]+"\n");
				}
			}
			fichero.write("10\n");
		}
		
		fichero.close();
	}
	
	public static CapaNeuronas[] leePesos(CapaNeuronas[] cps) throws FileNotFoundException{
		// Fichero del que queremos leer
		File fichero = new File("pesos_backup.txt");
		Scanner s = null;
		int sentinel = 0;
		
		s = new Scanner(fichero);
		float[][] pesosCapaOculta = new float[cps[0].getNumeroNeuronas()][cps[0].getNumeroEntradas()];
		float[][] pesosCapaSalida = new float[cps[1].getNumeroNeuronas()][cps[1].getNumeroEntradas()];
		
		int numeroNeurona = 0;
		int k = 0;
		// Leemos linea a linea el fichero
		while (s.hasNextLine()) {
			String linea = s.nextLine(); 	// Guardamos la linea en un String
		
			if(linea.equals("10")){
				sentinel++;
				numeroNeurona = 0;
				k=0;
			}else
				if(sentinel == 0){
					if(k == cps[0].getNumeroEntradas()){
						k = 0;
						numeroNeurona++;
					}
					pesosCapaOculta[numeroNeurona][k] = Float.parseFloat(linea);
					k++;
				}else
					if(sentinel == 1){
						if(k == cps[1].getNumeroEntradas()){
							k = 0;
							numeroNeurona++;
						}
						pesosCapaSalida[numeroNeurona][k] = Float.parseFloat(linea);
						k++;
					}
		}
		
		cps[0].setPesos(pesosCapaOculta);
		cps[1].setPesos(pesosCapaSalida);
		
		return cps;
	}
	
	public static void forwardPropagation(String conjunto, int id_Imagen){
		int images[][][];
		int labels[];
		
		
		if(conjunto.equals("Entrenamiento")){
			images = imagesTraining;
			labels = labelsTraining;
		}else{
			images = imagesTest;
			labels = labelsTest;
		}
		
		// Normalize image data
		float data[][] = mdb.normalize(images[id_Imagen]);

		//Limpiado de los valores de entrada de la anterior iteración.
		entrada.limpiar();
		oculta.limpiar();
		salida.limpiar();
			
		entradas = new float[28*28][1];
		int sentinel = 0;
			
		for(int i = 0; i < 28; i ++){
			for(int j = 0; j < 28; j++){
				entradas[sentinel][0] = data[i][j];
				sentinel++;
			}
		}
			
		salida.setSalidaDeseadas(labels[id_Imagen]);
			
		//Inicialización de la capa de entrada
		entrada.setEntradas(entradas);
		//Calculo de la salida de la capa de entrada
		entrada.calculaSalida();
		//Se propaga la salida a la capa siguiente
		entrada.propagarSalida();
			
		//Inicialización de la capa oculta
			
		//Calculo de la salida de la capa oculta
		oculta.calculaSalida();
		//Se propaga la salida a la capa final
		oculta.propagarSalida();
						
		//Calculo de la salida de la capa final
		salida.calculaSalida();
	}
	
	
	public static void aprende(int numeroEpocas) throws IOException{
		pj = new ArrayList<Float>();
		pj.add(1000.0f);
		
		//CONJUNTO DE ENTRENAMIENTO
		tamanioEjecucion = labelsTraining.length;
		for(int k = 0; k<numeroEpocas; k++){
			
			for(int m = 0; m< tamanioEjecucion ; m++){
				//Propagación hacia adelante para calcular la salida
				forwardPropagation("Entrenamiento",m);
					
				//Hora de calcular el error y ajustar los pesos de la capa inicial.
				salida.calculaError();
				salida.propagaError();
					
				//Hora de calcular el error y ajustar los pesos de la capa oculta.
				oculta.calculaError();
				oculta.ajustaPesos();
				salida.ajustaPesos();
				//Ya no se propaga el error más hacia atrás puesto que la capa inicial no ajusta sus pesos: son siempre 1.
			}
			System.out.println("Finalizada época "+(k+1));
					
			if(k>=0){
				//Ejecución sobre el CONJUNTO DE TEST
				errorAcumuladoTest = ejecuta("Test");
				//Ejecución sobre el CONJUNTO DE ENTRENAMIENTO
				ejecuta("Entrenamiento");
				
				Collections.sort(pj);
				
				//Sólo se almacenan pesos que hayan dado mejores resultados que los anteriores.
				if(pj.get(0) > errorAcumuladoTest){
					pj.add(errorAcumuladoTest);
					CapaNeuronas[] cps = new CapaNeuronas[2];
					cps[0] = oculta;
					cps[1] = salida;
					guardaPesos(cps);	
				}
			}
		}

	}
	
	public static float ejecuta(String sent){
		int images[][][];
		int labels[];
		
		
		if(sent.equals("Entrenamiento")){
			images = imagesTraining;
			labels = labelsTraining;
		}else{
			images = imagesTest;
			labels = labelsTest;
		}
				
		int tamanioEjecucion = images.length;
		
		float errorAcumulado = 0;
		for(int m = 0; m< tamanioEjecucion; m++){
			//Propagación hacia adelante para calcular la salida
			forwardPropagation(sent,m);
			if(salida.getValorDetectado() == labels[m]){
				errorAcumulado++;
			}
		}
			
		float error = (100.0f - (errorAcumulado*100/labels.length));
		System.out.println("Error sobre conjunto de "+sent+": "+error);
		
		return error;
	}
	
	public static void main(String[] args) throws IOException {
		boolean quieroAprender = true;	
		mdb = new MNISTDatabase();
		
		imagesTraining = mdb.readImages("src/data/"+trainingImages);
		labelsTraining = mdb.readLabels("src/data/"+trainingLabels);
		
		imagesTest = mdb.readImages("src/data/"+testImages);
		labelsTest = mdb.readLabels("src/data/"+testLabels);
	
		//Creación de la topología entre capas:
		entrada.setSinapsisSalida(oculta);
		oculta.setSinapsisEntrada(entrada);
		oculta.setSinapsisSalida(salida);
		salida.setSinapsisEntrada(oculta);
		
		File fichero = new File("pesos_backup.txt");
		if(fichero.exists() && !quieroAprender){
			CapaNeuronas[] cps = new CapaNeuronas[2];
			cps[0] = oculta;
			cps[1] = salida;
			entrada.setPesosDefault();
			oculta = leePesos(cps)[0];
			salida = leePesos(cps)[1];
		}else{
			entrada.setPesosDefault();
			oculta.setPesosDefault();
			salida.setPesosDefault();
		}
		
		aprende(35);
		//ejecuta("Test");
	}

}
