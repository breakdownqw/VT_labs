const CANVAS_ID = 'mainCanvas';
const CLEAR_BTN_ID = 'delete_button';
const LS_KEY = 'results';
const R_KEY = 'currentR';

let currentR = 1;

document.addEventListener('DOMContentLoaded', () => {
    const savedR = localStorage.getItem(R_KEY);
    if (savedR !== null) {
        currentR = Number(savedR) || 0;

        const rSel = document.getElementById("pointForm:rInput");
        if (rSel) {
            rSel.value = String(currentR);
        }
    }

    redraw();
    bindClickToCanvas();
    bindClearButton();
});


window.setR = function(newR) {
    currentR = Number(newR) || 0;

    if (Number.isFinite(currentR) && currentR > 0) {
        localStorage.setItem(R_KEY, String(currentR));
    } else {
        localStorage.setItem(R_KEY, "1");
    }

    redraw();
};

function redraw() {
    drawGraph(CANVAS_ID, currentR);
}

function drawGraph(canvasId, R) {
    const canvas = document.getElementById(canvasId);
    if (!canvas) return;
    canvas.width = canvas.clientWidth;
    canvas.height = canvas.clientHeight;

    const ctx = canvas.getContext('2d');
    const W = canvas.width, H = canvas.height;
    const margin = 40;
    const cx = W / 2, cy = H / 2;
    const WORLD_MAX = 5;
    const s = Math.min((W/2 - margin)/WORLD_MAX, (H/2 - margin)/WORLD_MAX);
    const X = x => cx + x * s;
    const Y = y => cy - y * s;

    ctx.clearRect(0, 0, W, H);

    ctx.fillStyle = '#777777';

    // === IV квадрант: прямоугольник x∈[0, r/2], y∈[-r, 0]
    ctx.beginPath();
    ctx.rect(X(0), Y(0), X(R/2) - X(0), Y(-R) - Y(0));
    ctx.fill();

    // === I квадрант: четверть круга, радиус r/2
    ctx.beginPath();
    ctx.moveTo(X(0), Y(0));
    ctx.arc(X(0), Y(0), s*(R/2), 0, -Math.PI/2, true);
    ctx.closePath();
    ctx.fill();

    // === II квадрант: треугольник с катетами r/2
    // Вершины:
    // (-r/2, 0)
    // (0, 0)
    // (-r/2, r)
    ctx.beginPath();
    ctx.moveTo(X(-R/2), Y(0));
    ctx.lineTo(X(0),     Y(0));
    ctx.lineTo(X(0), Y(R/2));
    ctx.lineTo(X(-R/2), Y(0));
    ctx.closePath();
    ctx.fill();

    drawAxesWhite(ctx, W, H, cx, cy, s, R, margin, X, Y, WORLD_MAX);
    drawPreviousPoints(ctx, X, Y, Number(R));
}

function drawAxesWhite(ctx, W, H, cx, cy, s, R, margin, X, Y, WORLD_MAX) {
    // Оси
    ctx.lineWidth = 1;
    ctx.strokeStyle = '#ffffff';
    ctx.beginPath();
    ctx.moveTo(margin/2, cy); ctx.lineTo(W - margin/2, cy);
    ctx.moveTo(cx, margin/2); ctx.lineTo(cx, H - margin/2);
    ctx.stroke();

    // Стрелки
    ctx.beginPath();
    ctx.moveTo(W - margin/2, cy); ctx.lineTo(W - margin/2 - 8, cy - 4);
    ctx.moveTo(W - margin/2, cy); ctx.lineTo(W - margin/2 - 8, cy + 4);
    ctx.moveTo(cx, margin/2); ctx.lineTo(cx - 4, margin/2 + 8);
    ctx.moveTo(cx, margin/2); ctx.lineTo(cx + 4, margin/2 + 8);
    ctx.stroke();

    // Сетка
    ctx.strokeStyle = '#444444'; ctx.lineWidth = 0.5;
    for (let i = -WORLD_MAX; i <= WORLD_MAX; i++) {
        ctx.beginPath(); ctx.moveTo(X(i), Y(-WORLD_MAX)); ctx.lineTo(X(i), Y(WORLD_MAX)); ctx.stroke();
        ctx.beginPath(); ctx.moveTo(X(-WORLD_MAX), Y(i)); ctx.lineTo(X(WORLD_MAX), Y(i)); ctx.stroke();
    }

    // Риски и подписи ±R, ±R/2
    ctx.lineWidth = 1; ctx.strokeStyle = '#00ff00'; ctx.fillStyle = '#00ff00'; ctx.font = '12px sans-serif';
    const special = [-R, -R/2, R/2, R];
    special.forEach(v => {
        if (v < -WORLD_MAX || v > WORLD_MAX) return;
        // X
        ctx.beginPath(); ctx.moveTo(X(v), cy - 6); ctx.lineTo(X(v), cy + 6); ctx.stroke();
        ctx.textAlign = 'center'; ctx.textBaseline = 'top'; ctx.fillText(lbl(v, R), X(v), cy + 16);
        // Y
        ctx.beginPath(); ctx.moveTo(cx - 6, Y(v)); ctx.lineTo(cx + 6, Y(v)); ctx.stroke();
        ctx.textAlign = 'right'; ctx.textBaseline = 'middle'; ctx.fillText(lbl(v, R), cx - 10, Y(v));
    });

    function lbl(val, Runit) {
        if (val === -Runit) return '-R';
        if (val === -Runit/2) return '-R/2';
        if (val ===  Runit/2) return 'R/2';
        if (val ===  Runit)   return 'R';
        return '';
    }
}

/* ====== Работа с точками и localStorage ====== */
function getPoints() {
    try { return JSON.parse(localStorage.getItem(LS_KEY)) || []; }
    catch { return []; }
}

function savePoint(p) {
    const data = getPoints();
    data.push(p);
    localStorage.setItem(LS_KEY, JSON.stringify(data));
}

function clearPoints() {
    localStorage.setItem(LS_KEY, JSON.stringify([]));
    redraw();
}

function drawPreviousPoints(ctx, X, Y, currentR) {
    const data = getPoints();
    if (!Array.isArray(data) || data.length === 0) return;

    for (const point of data) {
        const px = parseFloat(point.x);
        const py = parseFloat(point.y);
        const pr = parseFloat(point.r);

        // пересчёт старых R в текущий
        let k = 1;
        if (Number.isFinite(currentR) && currentR > 0 && Number.isFinite(pr) && pr > 0) {
            k = currentR / pr;
        }
        const xDraw = px * k;
        const yDraw = py * k;

        ctx.beginPath();
        ctx.arc(X(xDraw), Y(yDraw), 4, 0, 2 * Math.PI);
        ctx.fillStyle = (point.hit === true) ? 'lime' : 'red';
        ctx.fill();
        ctx.strokeStyle = '#000'; ctx.lineWidth = 1; ctx.stroke();
    }
}

/* ====== Клик по canvas: поставить точку и сохранить ====== */
function bindClickToCanvas() {
    const graphCanvas = document.getElementById(CANVAS_ID);
    if (!graphCanvas) return;

    graphCanvas.addEventListener('click', (event) => {
        if (!Number.isFinite(currentR) || currentR <= 0) return;

        const rect = graphCanvas.getBoundingClientRect();
        const clickX = event.clientX - rect.left;
        const clickY = event.clientY - rect.top;

        const W = graphCanvas.width, H = graphCanvas.height;
        const margin = 40;
        const cx = W / 2, cy = H / 2;
        const WORLD_MAX = 5;
        const s = Math.min((W/2 - margin)/WORLD_MAX, (H/2 - margin)/WORLD_MAX);

        // canvas -> математические координаты
        const x = (clickX - cx) / s;
        const y = (cy - clickY) / s;

        const xFinal = Math.round(x);
        const yFinal = Number(y.toFixed(3));

        const point = {
            x: xFinal,
            y: yFinal,
            r: currentR,
            hit: isHit(xFinal, yFinal, currentR),
            nowTime: new Date().toLocaleString()
        };
        let log = sendPointToServerFromCanvas(xFinal, yFinal, currentR);
        showError(log);

        if (log && log.length > 0) {
            return;
        }
        savePoint(point);
        redraw();
    });
}

/* ====== Очистка localStorage кнопкой ====== */
function bindClearButton() {
    const btn = document.getElementById(CLEAR_BTN_ID);
    if (btn) btn.addEventListener('click', clearPoints);
}

/* ====== Геометрия «попадания» (как в вашей области) ====== */
function isHit(x, y, r) {
    if (r <= 0) return false;

    // IV квадрант — прямоугольник
    const inRect = (x >= 0 && x <= r/2 && y <= 0 && y >= -r);

    // I квадрант — четверть круга
    const inArc = (x >= 0 && y >= 0 && (x*x + y*y) <= (r/2)*(r/2));

    // II квадрант — треугольник (как в Java)
    // y <= 2(x + r/2), x ∈ [-r/2, 0], y ≥ 0
    const inTriangle =
        (x <= 0 && x >= -r/2 &&
            y >= 0 && y <= 2*(x + r/2));

    return inRect || inArc || inTriangle;
}

window.addPointFromForm = function () {
    const formId = 'pointForm';
    const xSel   = document.getElementById(formId + ':xInput');
    const yInput = document.getElementById(formId + ':yInput');
    const rSel   = document.getElementById(formId + ':rInput');

    if (!xSel || !yInput || !rSel) return;

    const x = Number(xSel.value);
    const y = Number(String(yInput.value));
    const r = Number(rSel.value);

    if (!Number.isFinite(x) || !Number.isFinite(y) || !Number.isFinite(r) || r <= 0) {
        return;
    }

    const point = {
        x: x,
        y: y,
        r: r,
        hit: isHit(x, y, r),
        nowTime: new Date().toLocaleString()
    };

    savePoint(point);
    redraw();
};

function sendPointToServerFromCanvas(x, y, r) {
    const formId = 'pointForm';
    let log = [];
    const xSel = document.getElementById(formId + ':xInput');
    const yInput = document.getElementById(formId + ':yInput');
    const rSel = document.getElementById(formId + ':rInput');
    const hiddenBtn = document.getElementById(formId + ':hiddenSubmit');

    if (!xSel || !yInput || !rSel || !hiddenBtn) {
        console.warn('Не нашёл один из элементов формы для отправки точки');
        log.push("Внутренняя ошибка: элементы формы для отправки точки не найдены");
        return log;
    }
    validateAll(x, y, r, log)

    if (log.length > 0) return log;

    xSel.value = Math.round(x);
    yInput.value = y.toFixed(3);
    rSel.value = r;

    console.log('Отправляю точку с canvas:', xSel.value, yInput.value, rSel.value);
    hiddenBtn.click();
    return [];
}

function validateAll(x, y, r, log) {
    validateX(x, log);
    validateY(y, log);
    validateR(r, log);
}

function validateX(x, log) {
    const xRange = [-5, -4, -3, -2, -1, 0, 1, 2, 3]; // подстрой под свой select
    if (!xRange.includes(x)) {
        log.push("Значение для X должно быть выбрано из допустимых значений");
    }
}

function validateY(y, log) {
    if (!(y >= -5 && y <= 5)) {
        log.push("Значение для Y должно быть в диапазоне [-5, 5]");
    }
}

function validateR(r, log) {
    if (!(r >= 1 && r <= 4)) { // или rRange.includes(r), если R дискретный
        log.push("Значение для R должно быть в диапазоне [1, 4]");
    }
}

function showError(log) {
    const el = document.getElementById("pointForm:msgs")
        || document.getElementById("msgs");

    el.innerHTML = "";

    if (!log || log.length === 0) {
        return;
    }

    const arr = Array.isArray(log) ? log : [log];

    for (let i = 0; i < arr.length; i++) {
        const line = document.createElement("div");
        line.textContent = `${i + 1}. ${arr[i]}`;
        line.style.color = "red";
        el.appendChild(line);
    }
}

