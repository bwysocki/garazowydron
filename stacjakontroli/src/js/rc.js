import $ from "jquery";
import * as THREE from "three";
import { GLTFLoader } from 'three/examples/jsm/loaders/GLTFLoader.js';

$(document).ready(() => {
    const ROTATION_HTML_ELEMENT = $("#rotation");

    ROTATION_HTML_ELEMENT.load('/src/html/rotation.html', () => {
        console.log('Formularz kwaternionu załadowany');

        // Pobierz aktualną szerokość i wysokość
        const container = ROTATION_HTML_ELEMENT.get(0);
        const width = container.clientWidth;
        const height = container.clientHeight - 40;

        // Inicjalizacja sceny i kamery z dopasowanymi wymiarami
        const scene = setupScene();
        const camera = setupCamera(width, height);
        const renderer = setupRenderer(container, width, height);

        let planeModel = null;
        const initialQuaternion = new THREE.Quaternion(0, 0, 0, 1);

        // Wczytaj model samolotu
        loadPlaneModel(scene).then((model) => {
            planeModel = model; // przypisz do zmiennej globalnej dla późniejszego użycia
            planeModel.scale.set(0.35, 0.35, 0.35);
            planeModel.setRotationFromQuaternion(initialQuaternion);
        });

        // Renderuj wizualizację
        requestAnimationFrame(function animate() {
            requestAnimationFrame(animate);
            renderer.render(scene, camera);
        });

        // Events:
        $('#quat-form').on('submit', function (e) {
            e.preventDefault();
            const w = parseFloat($('#w').val());
            const x = parseFloat($('#x').val());
            const y = parseFloat($('#y').val());
            const z = parseFloat($('#z').val());

            updatePlaneRotation(w,x,y,z, planeModel);
        });

        const socket = new WebSocket("ws://localhost:8085/ws/drone");
        socket.onmessage = (event) => {
            try {
                const mavlinkMsg = JSON.parse(event.data);
                if (mavlinkMsg.msgid === 31) {
                    $('#w').val(mavlinkMsg.q1);
                    $('#x').val(mavlinkMsg.q2);
                    $('#y').val(mavlinkMsg.q3);
                    $('#z').val(mavlinkMsg.q4);
                    updatePlaneRotation(mavlinkMsg.q1, mavlinkMsg.q2, mavlinkMsg.q3, mavlinkMsg.q4, planeModel);
                }
            } catch (e) {
                console.error("❌ Niepoprawny JSON:", event.data);
            }
        };

    });
});


function updatePlaneRotation(w, x, y, z, planeModel) {
    // TODO: Zamiana y <-> z jeśli model ma osie zamienione
    const quaternion = new THREE.Quaternion(x, z, y, w).normalize();

    if (planeModel) {
        planeModel.setRotationFromQuaternion(quaternion);
    } else {
        console.warn("Model jeszcze się nie załadował.");
    }
}

/**
 * Dodaje siatkę, osie pomocnicze oraz jasne tło do sceny.
 *
 */
function setupScene() {
    const scene = new THREE.Scene();
    scene.background = new THREE.Color(0xf0f0f0); // Jasnoszare tło

    const gridHelper = new THREE.GridHelper(10, 10);
    scene.add(gridHelper);

    // Osie: X (czerwona), Y (zielona), Z (niebieska)
    const axesHelper = new THREE.AxesHelper(5); // długość osi = 5 jednostek
    scene.add(axesHelper);

    const ambientLight = new THREE.AmbientLight(0xffffff, 0.6); // światło ogólne
    scene.add(ambientLight);

    const directionalLight = new THREE.DirectionalLight(0xffffff, 0.8); // światło kierunkowe
    directionalLight.position.set(5, 10, 7);
    scene.add(directionalLight);

    return scene;
}

/**
 * Tworzy kamerę perspektywiczną dopasowaną do rozmiaru kontenera.
 *
 * @param {number} width - szerokość kontenera (canvasu)
 * @param {number} height - wysokość kontenera (canvasu)
 * @returns {THREE.PerspectiveCamera} - gotowa do użycia kamera
 */
function setupCamera(width, height) {

    // Tworzy kamerę perspektywiczną (jak ludzkie oko) z parametrami:
    // 75        → kąt widzenia (FOV – field of view) w pionie, w stopniach
    // width / height → proporcje widoku (aspect ratio), aby obraz nie był rozciągnięty
    // 0.1       → najbliższa odległość renderowania (near clipping plane)
    // 1000      → najdalsza odległość renderowania (far clipping plane)
    //
    // Obiekty bliższe niż 0.1 jednostki lub dalsze niż 1000 nie będą widoczne.
    const camera = new THREE.PerspectiveCamera(75, width / height / 2, 0.1, 1000);

    camera.position.set(2, 4, 4);
    camera.lookAt(0, 0, 0);

    return camera;
}

/**
 * Tworzy renderer WebGL z włączonym antyaliasingiem i osadza go w kontenerze DOM.
 *
 * @param {HTMLElement} container - element DOM, w którym ma być umieszczony canvas renderera
 * @param {number} width - szerokość renderera
 * @param {number} height - wysokość renderera
 * @returns {THREE.WebGLRenderer} - gotowy renderer Three.js
 */
function setupRenderer(container, width, height) {
    const renderer = new THREE.WebGLRenderer({ antialias: true }); // wygładzanie krawędzi
    renderer.setSize(width, height); // ustaw rozmiar canvasu
    container.appendChild(renderer.domElement); // osadź canvas w kontenerze

    return renderer;
}


/**
 * Wczytuje model samolotu z pliku GLB i dodaje go do sceny.
 *
 * @param {THREE.Scene} scene - scena Three.js
 * @param {string} url - ścieżka do pliku .glb
 * @returns {Promise<THREE.Group>} - obiekt 3D samolotu
 */
function loadPlaneModel(scene, url = '/src/models/cirrus_sr22.glb') {
    return new Promise((resolve, reject) => {
        const loader = new GLTFLoader();

        loader.load(
            url,
            (gltf) => {
                const model = gltf.scene;
                const wrapper = new THREE.Group();
                wrapper.add(model);
                wrapper.scale.set(1, 1, 1);
                wrapper.position.set(0, 0, 0);
                scene.add(wrapper);
                resolve(wrapper);
            },
            undefined,
            (error) => {
                console.error('Błąd ładowania modelu:', error);
                reject(error);
            }
        );
    });
}