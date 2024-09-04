window.AudioContext = window.AudioContext || window.webkitAudioContext || null;
var context = new AudioContext();

var context = null;
var usingWebAudio = null;

try {
    if (typeof AudioContext !== 'undefined') {
        context = new AudioContext();
    } else if (typeof webkitAudioContext !== 'undefined') {
        context = new webkitAudioContext();
    } else {
        usingWebAudio = false;
    }
} catch (e) {
    usingWebAudio = false;
};

if (usingWebAudio && context.state === 'suspended') {
    var resume = function () {
        context.resume();
        setTimeout(function () {
            if (context.state === 'running') {
                document.body.removeEventListener('touchend', resume, false);
            }
        }, 0);
    };
    document.body.addEventListener('touchend', resume, false);
};

var timer, noteCount, accentPitch = 380, offBeatPitch = 200;
var curTime = 0.0;
var beatsPerMeasure = 4;
var isPlaying = false;

function schedule() {
    while (curTime < context.currentTime + 0.1) {
        playNote(curTime);
        updateTime();
    }
    timer = window.setTimeout(schedule, 0.1);
}

function updateTime() {
    curTime += 60.0 / parseInt($(".bpm-input").val(), 10);
    noteCount++;
}

/* Play note on a delayed interval of t */
function playNote(t) {
    var note = context.createOscillator();

    if (noteCount == beatsPerMeasure)
        noteCount = 0;

    if (noteCount === 0)
        note.frequency.value = accentPitch;
    else
        note.frequency.value = offBeatPitch;

    note.connect(context.destination);

    note.start(t);
    note.stop(t + 0.05);

    if (noteCount === 0)
        $(".next-chord-button").click()
}

$(".metronome-toggle").click(function () {
    if (isPlaying) {
        isPlaying = false;
        window.clearInterval(timer);
    } else {
        isPlaying = true;
        curTime = context.currentTime;
        noteCount = beatsPerMeasure;
        schedule();
    }
});

window.addEventListener('keydown', function (e) {
    if (e.code === 'Space') {
        const element = document.querySelector('.metronome-button');
        if (element) {
            element.click();
        }
        console.log('Space pressed');
    }
});
