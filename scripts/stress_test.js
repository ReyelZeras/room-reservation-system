/**
 * RoomRes - Stress Test Engine
 * Execução: node scripts/stress_test.js
 * * Este script dispara requisições simultâneas contra o API Gateway para testar
 * o Service Discovery, Circuit Breaker (Resilience4j) e Cache (Redis).
 */

const http = require('http');

// ==========================================
// ⚠️ CONFIGURAÇÕES (PREENCHA AQUI)
// ==========================================
// 1. Vá ao Front-end, faça Login e copie o seu Token JWT.
const TOKEN = "COLE_O_SEU_TOKEN_AQUI_ENTRE_AS_ASPAS";

// 2. Parâmetros do Ataque
const TOTAL_REQUESTS = 5000; // Quantidade de "tiros" que vamos dar
const CONCURRENCY = 100;     // Quantas requisições simultâneas de cada vez

// ==========================================

const TARGET_URL = 'http://localhost:8080/api/v1/bookings/availability?start=2026-10-10T14:00:00&end=2026-10-10T16:00:00';

if (TOKEN === "COLE_O_SEU_TOKEN_AQUI_ENTRE_AS_ASPAS") {
    console.error("❌ ERRO: Você precisa colar o seu Token JWT no arquivo stress_test.js antes de rodar!");
    process.exit(1);
}

console.log(`\n🚀 INICIANDO TESTE DE CARGA (PROFILING)`);
console.log(`Alvo: ${TARGET_URL}`);
console.log(`Disparos: ${TOTAL_REQUESTS} | Concorrência: ${CONCURRENCY}\n`);

let completed = 0;
let successful = 0;
let failed = 0;
const startTime = Date.now();

function makeRequest() {
    return new Promise((resolve) => {
        const req = http.get(TARGET_URL, {
            headers: { 'Authorization': `Bearer ${TOKEN}` }
        }, (res) => {
            // Consome os dados para libertar a memória
            res.on('data', () => {});
            res.on('end', () => {
                if (res.statusCode === 200) successful++;
                else failed++;
                resolve();
            });
        });

        req.on('error', (err) => {
            failed++;
            resolve();
        });

        req.end();
    });
}

async function run() {
    const batches = Math.ceil(TOTAL_REQUESTS / CONCURRENCY);

    for (let i = 0; i < batches; i++) {
        const promises = [];
        const currentBatchSize = Math.min(CONCURRENCY, TOTAL_REQUESTS - completed);

        for (let j = 0; j < currentBatchSize; j++) {
            promises.push(makeRequest());
        }

        await Promise.all(promises);
        completed += currentBatchSize;

        // Log de progresso
        if (completed % 500 === 0 || completed === TOTAL_REQUESTS) {
            process.stdout.write(`Progresso: ${completed} / ${TOTAL_REQUESTS} requisições...\r`);
        }
    }

    const endTime = Date.now();
    const durationSec = (endTime - startTime) / 1000;
    const rps = (TOTAL_REQUESTS / durationSec).toFixed(2);

    console.log(`\n\n📊 RELATÓRIO DO TESTE DE CARGA`);
    console.log(`================================`);
    console.log(`Tempo Total : ${durationSec} segundos`);
    console.log(`Vazão (RPS) : ${rps} requisições/segundo`);
    console.log(`Sucesso 🟢  : ${successful}`);
    console.log(`Falhas  🔴  : ${failed}`);
    console.log(`================================`);
    console.log(`Vá ao Grafana e veja os gráficos de CPU e Requests dispararem!`);
}

run();