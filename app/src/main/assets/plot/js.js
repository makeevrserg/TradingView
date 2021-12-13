
class Data {
    constructor() {
        var min = 300;
        var max = 600;
        this.date = "2021-09-21T17:00:00.000Z";
        this.high = Math.random() * (max - min) + min;
        this.low = Math.random() * (this.high - min) + min;
        max = this.high;
        min = this.low;
        this.close = Math.random() * (max - min) + min;
        this.open = Math.random() * (this.close - min) + min;
    }
}



const canvas = document.querySelector("#plot");
const ctx = canvas.getContext("2d");
const lowColor = "#FF2929"
const highColor = "#2962FF"
const xStep = 60;
const xWidth = 30;
var data = [new Data()]
var minMax = getMinMax()
var min = 0;
var max = 0;


/**
 * Апдейтим min и max
 */
function updateMinMax() {
    var minMax = getMinMax()
    min = minMax.min;
    max = minMax.max;
}
/**
 * Тут загружаются данные. Через Android WebView передаем сконвертированные через Gson данные
 */
function _loadData(updatedData) {
    if (updatedData != null)
        data = updatedData
    console.log(updatedData)
    for (let i = 0; i < 50; i++) {
        data.push(new Data(i))
    }
    updateMinMax()
    render()
    console.log('Loading data')
    return 50
}


/**
 * получем минимальные и максимальные значения чтобы уставноить ширину и высоту канваса
 */
function getMinMax() {
    var lowest = Number.POSITIVE_INFINITY;
    var highest = Number.NEGATIVE_INFINITY;
    for (var i = 0; i < data.length; i++) {
        var d = data[i]
        a = [d.high, d.low, d.close, d.open]
        var max = Math.max.apply(null, a);
        var min = Math.min.apply(null, a);
        if (max > highest) highest = max
        if (min < lowest) lowest = min
    }
    return { min: lowest, max: highest }
}


/**
 * Рисуется график
 */
function drawData() {
    var x = 0;
    for (let i = 0; i < data.length - 1; ++i) {
        var d = data[i]
        if (data[i].close <= data[i + 1].close)
            ctx.fillStyle = highColor;
        else
            ctx.fillStyle = lowColor;

        ctx.fillRect(getX(x), getY(Math.min(d.open, d.close)), getX(xWidth), -Math.abs(d.open - d.close))
        ctx.fillRect(getX(x + xStep / 5), getY(d.low), getX(xWidth / 4), -Math.abs(d.low - d.high));
        x += xStep;
    }
}

/**
 * Преобразуем координату y канваса в декартову
 */
function getY(val) {
    // return (canvas.height - val)
    return (canvas.height - ((canvas.height) / max * val))
}
/**
 * Преобразуем координату x канваса в декартову
 */
function getX(val) {
    return val
}
/**
 * Рисуем текст с ценами
 */
function drawText() {
    var fontSize = 24
    ctx.font = fontSize + "px sans";
    ctx.fillStyle = "#909090";
    for (let i = min; i < max; i += 28) {
        ctx.fillText((Math.round((i) * 100) / 100), getX(window.innerWidth + this.window.scrollX - 100), getY(i));
    }

}
function getDate(date) {
    var d = date.split('T')[1].split(':')
    return d[0] + ":" + d[1]
}
function drawDate() {
    var fontSize = 24
    ctx.font = fontSize + "px sans";
    ctx.fillStyle = "#909090";
    var x = 0;
    for (let i = 0; i < data.length - 1; ++i) {
        var d = data[i]
        var y = window.innerHeight + window.scrollY - 30;
        if (y > canvas.height)
            y = canvas.height
        ctx.fillText(getDate(d.date), getX(x), y);
        x += xStep + 20
    }

}

/**
 * Ставим хендлер на ресайз картинки
 */
function resize() {
    canvas.height = (min + max);
    canvas.width = (data.length * xStep);
}
/**
 * Рендерим график и текст
 */
function render() {
    // context.clearRect(0, 0, canvas.width, canvas.height);
    resize()
    drawData()
    drawText()
    drawDate()
}
window.addEventListener('load', () => {
    console.log('load')
    render()
})
window.addEventListener('scroll', function (e) {
    console.log('scroll')
    render()
});
window.addEventListener('resize', () => {
    console.log('resize')

    render()
}, true)

_loadData(null)