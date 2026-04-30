/**
 * RoomRes - Stress Test Engine (BOOKING CREATION)
 * Execução: node scripts/stress_test_bookings.js
 * * Este script testa a ESCRITA no banco de dados, concorrência,
 * e a carga nas filas do Kafka e RabbitMQ.
 */

const http = require('http');

// ==========================================
// ⚠️ CONFIGURAÇÕES (PREENCHA AQUI)
// ==========================================
// 1. O seu Token JWT válido
const TOKEN = "COLE_O_SEU_TOKEN_AQUI_ENTRE_AS_ASPAS";

// 2. O UUID real do seu usuário (pegue no banco ou na tela de perfil)
const USER_ID = "COLE_O_SEU_TOKEN_AQUI_ENTRE_AS_ASPAS";

// 3. O UUID real de uma sala existente
const ROOM_ID = "COLE_O_SEU_TOKEN_AQUI_ENTRE_AS_ASPAS";

const TOTAL_REQUESTS = 100;
const CONCURRENCY = 20;

// ==========================================

if (TOKEN === "COLE_O_SEU_TOKEN_AQUI_ENTRE_AS_ASPAS" || USER_ID.includes("COLE") || ROOM_ID.includes("COLE")) {
    console.error("❌ ERRO: Por favor, preencha o Token, o USER_ID e o ROOM_ID no arquivo!");
    process.exit(1);
}

console.log(`\n🚀 INICIANDO TESTE DE CARGA (CRIAÇÃO DE RESERVAS)`);
console.log(`Disparos: ${TOTAL_REQUESTS} | Concorrência: ${CONCURRENCY}\n`);

let completed = 0;
let successCreated = 0;
let errorConflict = 0;
let errorServer = 0;
const startTime = Date.now();

function makeBookingRequest(index) {
    return new Promise((resolve) => {
        // Gera reservas para daqui a 10 dias, espaçadas de hora em hora
        const start = new Date();
        start.setDate(start.getDate() + 10);
        start.setHours(start.getHours() + index);

        const end = new Date(start);
        end.setHours(start.getHours() + 1);

        // CORREÇÃO: Enviando o userId e o roomId como Strings (UUID)
        const payload = JSON.stringify({
            userId: USER_ID,
            roomId: ROOM_ID,
            title: `Stress Test Booking #${index}`,
            startTime: start.toISOString(),
            endTime: end.toISOString()
        });

        const options = {
            hostname: 'localhost',
            port: 8080,
            path: '/api/v1/bookings',
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${TOKEN}`,
                'Content-Type': 'application/json',
                'Content-Length': Buffer.byteLength(payload)
            }
        };

        const req = http.request(options, (res) => {
            res.on('data', () => {});
            res.on('end', () => {
                if (res.statusCode === 201 || res.statusCode === 200) {
                    successCreated++;
                } else if (res.statusCode === 400 || res.statusCode === 422 || res.statusCode === 409) {
                    errorConflict++; // Regra de negócio barrou (ex: Sala Inexistente ou Ocupada)
                } else {
                    errorServer++; // Erros 500 reais
                }
                resolve();
            });
        });

        req.on('error', (err) => {
            errorServer++;
            resolve();
        });

        req.write(payload);
        req.end();
    });
}

async function run() {
    const batches = Math.ceil(TOTAL_REQUESTS / CONCURRENCY);

    for (let i = 0; i < batches; i++) {
        const promises = [];
        const currentBatchSize = Math.min(CONCURRENCY, TOTAL_REQUESTS - completed);

        for (let j = 0; j < currentBatchSize; j++) {
            promises.push(makeBookingRequest(completed + j));
        }

        await Promise.all(promises);
        completed += currentBatchSize;
        process.stdout.write(`Progresso: ${completed} / ${TOTAL_REQUESTS} reservas tentadas...\r`);
    }

    const durationSec = (Date.now() - startTime) / 1000;

    console.log(`\n\n📊 RELATÓRIO: CRIAÇÃO DE RESERVAS`);
    console.log(`================================`);
    console.log(`Tempo Total : ${durationSec} segundos`);
    console.log(`Criadas 🟢  : ${successCreated} (O RabbitMQ e o Kafka receberam mensagens!)`);
    console.log(`Negadas 🟡  : ${errorConflict} (Regras de negócio funcionaram)`);
    console.log(`Erros   🔴  : ${errorServer} (Falhas de banco/servidor)`);
    console.log(`================================`);
}

run();