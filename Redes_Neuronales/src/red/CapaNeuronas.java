package red;

import java.util.Random;

public class CapaNeuronas {
	private float[][] entradas;
	private float[][] pesos; //Se establecen de inicio en función del número de entradas
	private float[] salidas;
	private float[] salidasDeseadas;//Se introduce desde el main
	private float tasaAprendizaje;
	private String nombre;
	private float[][] errorPesos;
	private float[][] errorPesosAnterior;
	private CapaNeuronas sinapsisSalida;
	private CapaNeuronas sinapsisEntrada;
	private float[] errorCapaSuperior;
	private int numeroNeuronas;
	private int numeroEntradas;
	private float[] errorEntrada;
	private float valorDetectado;
	private float deseado;
	private float[] ganancia;
	private float momentum;
	private float[][] aPropagar;
	private float[] errorSalida;
	private float pesoAsociado;
	private float valor_mayor;
	private float index_mayor;
	private Random r;
	
	public CapaNeuronas(String nombre, int numero_neuronas, int numero_entradas){
		entradas = new float[numero_neuronas][numero_entradas];
		pesos = new float[numero_neuronas][numero_entradas];
		errorPesos = new float[numero_neuronas][numero_entradas];
		salidas = new float[numero_neuronas];
		this.nombre = nombre;
		numeroNeuronas = numero_neuronas;
		numeroEntradas = numero_entradas;
		ganancia = new float[numero_neuronas];
		tasaAprendizaje = 0.1f;
		momentum = 0.9f;
		
		for(int i = 0; i<numero_neuronas;i++){
			for(int j = 0;j<numero_entradas;j++){
				errorPesos[i][j] = 0.0f;
			}
		}
	}
	
	public void setSalidaDeseadas(float deseado){
		this.deseado = deseado;
		
		salidasDeseadas = new float[10];
		for(int i = 0; i<10; i++){
			if(i == deseado)
				salidasDeseadas[i] = 1.0f;
			else
				salidasDeseadas[i] = 0.0f;
		}
	}
	
	public void setEntradas(float[][] ents){
		entradas = ents;
	}
	
	public void setErrorCapaSuperior(float[] errorSuperior){
		errorCapaSuperior = errorSuperior;
	}
	
	public float funcionActivacion(float res){
		return (1.0f / (1.0f + (float)Math.exp(-res)));
	}
	
	public float[][] getPesos(){
		return pesos;
	}
	
	public int getNumeroNeuronas(){
		return numeroNeuronas;
	}
	
	public int getNumeroEntradas(){
		return numeroEntradas;
	}
	
	public void setSinapsisSalida(CapaNeuronas sinap){
		sinapsisSalida = sinap;
	}
	
	public void setSinapsisEntrada(CapaNeuronas sinap){
		sinapsisEntrada = sinap;
	}
	
	public float[] getSalida(){
		return salidas;
	}
	
	
	//Se propaga el mismo vector de salida para las n neuronas de la sinapsis de salida de esta capa.
	//Porque todas las neuronas de esta capa están conectadas con las neuronas de la siguiente capa.
	public void propagarSalida(){
		aPropagar = new float[sinapsisSalida.getNumeroNeuronas()][numeroNeuronas];
		for(int i=0; i<sinapsisSalida.getNumeroNeuronas(); i++){
			for(int  k = 0; k<numeroNeuronas; k++){
				aPropagar[i][k] = salidas[k];	
			}
		}
		sinapsisSalida.setEntradas(aPropagar);
	}
	
	public String getNombre(){
		return nombre;
	}
	
	public void calculaError(){//El error debe ser en valor absoluto para no crear conflictos.
		errorSalida = new float[salidas.length];
		errorEntrada = new float[salidas.length];
		
		errorPesosAnterior = errorPesos;
		
		if(nombre.equals("salida")){ //Se trata de la capa de salida.
			for(int i = 0; i<numeroNeuronas; i++){
				//Derivada del error con respecto a la salida
				errorSalida[i] = (salidasDeseadas[i] - salidas[i]);
				
				//Derivada del error con respecto a la entrada
				errorEntrada[i] = salidas[i]*(1.0f-salidas[i])*errorSalida[i];
				
				for(int k = 0; k<numeroEntradas; k++){
					errorPesos[i][k] = (tasaAprendizaje*sinapsisEntrada.getSalida()[k]*errorEntrada[i]) + momentum*errorPesos[i][k]; //Es el errorEntrada lo que se usa en vez del errorCapaSuperior porque se trata de la capa final y no hay superior.
				}
			}
			//Se propaga el error con respecto a la entrada, porque es lo que se utiliza en las capas inferiores.
			sinapsisEntrada.setErrorCapaSuperior(errorEntrada);
		}else{ //Se trata de la capa intermedia: No se propaga errores a la capa inicial. Y desde la inicial tampoco: Porque no hay más.
			for(int i = 0; i<numeroNeuronas; i++){
				errorSalida[i] = 0.0f;
				
				//Derivada del error con respecto a la salida
				for(int k = 0; k<sinapsisSalida.getNumeroNeuronas(); k++){
					pesoAsociado = sinapsisSalida.getPesos()[k][i];
					errorSalida[i] = errorSalida[i] + (pesoAsociado*errorCapaSuperior[k]);
				}
				
				//Derivada del error con respecto a la entrada
				errorEntrada[i] = salidas[i]*(1-salidas[i])*errorSalida[i];
				
				for(int k = 0; k<numeroEntradas;k++){
					errorPesos[i][k] = (tasaAprendizaje*sinapsisEntrada.getSalida()[k]*errorEntrada[i]) + momentum*errorPesos[i][k];
				}
			}
		}
		
	}

	public void ajustaPesos(){
		for(int i = 0; i<numeroNeuronas; i++){
			for(int k = 0; k<numeroEntradas; k++){
				if((pesos[i][k] + errorPesos[i][k]) >= -1.0f && (pesos[i][k] + errorPesos[i][k])<=1.0f){
					pesos[i][k] = pesos[i][k] + errorPesos[i][k];
				}
			}
		}
	}
	
	public void propagaError(){
		sinapsisEntrada.setErrorCapaSuperior(errorEntrada);
	}
	
	public void calculaSalida(){
		valor_mayor = 0.0f;
		index_mayor = 0.0f;
		
		for(int i = 0; i<numeroNeuronas; i++){
			salidas[i] = 0.0f;
			for(int k = 0; k<numeroEntradas; k++){
				salidas[i] = salidas[i] + (pesos[i][k]*entradas[i][k]);
			}
			
			if(!nombre.equals("entrada")){
				salidas[i] = funcionActivacion(salidas[i]);
			}
			
			
			if(nombre.equals("salida")){
				if(salidas[i] > valor_mayor){
					valor_mayor = salidas[i];
					index_mayor = i;
					valorDetectado = index_mayor;
				}
			}
		}
		
	}
	
	public double getValorDetectado(){
		return valorDetectado;
	}
	
	//Se fijan los pesos iniciales en función de las entradas de las neuronas en sinapsis de entrada.
	public void setPesosDefault(){
		r = new Random();
		
		for(int i = 0; i<numeroNeuronas; i++){
			for(int k = 0; k < numeroEntradas; k++ ){
				if(nombre.equals("entrada")){
					pesos[i][k] = 1.0f;
				}else
					pesos[i][k] = (-1.0f + (1.0f - (-1.0f) ) * r.nextFloat());
			}
		}
	}
	
	public void setPesos(float[][] pes){
		pesos = pes;
	}
	
	public void limpiar(){
		entradas = null;
	}
}
