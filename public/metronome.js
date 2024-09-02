window.AudioContext = window.AudioContext || window.webkitAudioContext;
var context = new AudioContext();
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

window.addEventListener('keydown', function(e) {
    if (e.code === 'Space') {
        const element = document.querySelector('.metronome-button');
        if (element) {
            element.click();
        }
        console.log('Space pressed');
    }
});
