import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import javafx.util.Pair;

public class Main {
	private static int[][] matrizPesos;
	private static int[][] matrizDistancias;
	private static int tamanioPoblacion;
	private static int numeroGeneraciones;
	private static int N;
	
	public static void leeMatrices(String nombreDatos) throws IOException{
		List<String> lineas = Files.readAllLines(new File(nombreDatos).toPath());
		N = Integer.parseInt(lineas.get(0).split(" ")[lineas.get(0).split(" ").length-1]);
		
		matrizPesos = new int[N][N];
		matrizDistancias = new int[N][N];
		boolean sentinel = false ; //Para diferenciar qué matriz estamos rellenando: pesos o distancias.
		int[] fil;
		
		
		//Rellenamos las matrices de peso y distancia.///////////////////////////////////////////////////////////
		//Empezamos desde la segunda linea porque las dos primeras tienen el tamaño de las matrices y un espacio.
		for(int i=2; i<lineas.size(); i++){
			String[] fila = lineas.get(i).split(" ");
			fil = new int[N];
					
			if(nombreDatos.equals("src/datos/tai256c.dat")){
				if(i-2 == N){
					sentinel = true;
				}
						
				if((i-3-N) < N){
					int index = 0;
					for(int j=0;j<fila.length;j++){
						if(!fila[j].equals("") && !fila[j].equals(" ")){
							fil[index] = Integer.parseInt(fila[j]);
							index++;
						}
					}
							
					if(sentinel){
						matrizDistancias[i-2-N] = fil;
					}else{
						matrizPesos[i-2] = fil;
					}
				}
			}else{
				if(i-2 == N){
					sentinel = true;
				}else
					if((i-3-N) < N){
						int index = 0;
						for(int j=0;j<fila.length;j++){
							if(!fila[j].equals("") && !fila[j].equals(" ")){
								fil[index] = Integer.parseInt(fila[j]);
								index++;
							}
						}
							
						if(sentinel){
							matrizDistancias[i-3-N] = fil;
						}else{
							matrizPesos[i-2] = fil;
						}
					}
			}
		}
	}
	
	public static void main(String[] args) throws IOException {
		
		//Nombre del paquete de datos de prueba a usar
		String nombreDatos = "src/datos/tai256c.dat";
		tamanioPoblacion = 100;
		numeroGeneraciones = 100;
		ArrayList<Pair<Integer,Integer>> padres;
		//////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		//Se leen los datos
		leeMatrices(nombreDatos);
		
		//Se crea la población
		Poblacion p = new Poblacion(N,tamanioPoblacion,matrizDistancias,matrizPesos,TipoAlgoritmo.Lamarck);
		
		//Comienzo de la ejecución del algoritmo genético//////////////////////////////////////////////////////////////////////
		p.inicializa();
		p.evalua();
		
		for(int k = 0;k<numeroGeneraciones;k++){
			padres = p.selecciona((k+1));
			p.cruza(padres);
			p.mutar();
			p.evalua();
		}
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		//Salida de los resultados.
		System.out.println("En la generación: "+p.getGeneracionIdeal()+" se ha obtenido el menor peso: "+BigDecimal.valueOf(p.getMenorPeso()));
		int[] mejorPermutacion = p.getMejorPermutacion();
		
		System.out.println("Mejor Permutacion: ");
		for(int i = 0; i<mejorPermutacion.length; i++){
			System.out.print(mejorPermutacion[i]+" ");
		}
		System.out.println();
	}

}
