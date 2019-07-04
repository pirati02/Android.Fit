package com.mygpi.mygpimobilefitness.api

import java.math.BigDecimal

internal class StepDetector(private val stepListener: StepListener) {
    private val initVel = 1.1f
    private val fourVels = floatArrayOf(initVel, initVel, initVel, initVel,
            initVel, initVel, initVel, initVel, initVel,
            initVel, initVel, initVel, initVel, initVel, initVel)
    private var pos = 0
    private var lastStepTime: Long = 0
    private var lastVel = 0f

    private val WAIT_MODEL = 0
    private val RUN_MODEL = 2
    private var model: Int = 0

    private var tempSteps: Long = 0
    private var initCount: Int = 0
    private var crest = 0f
    private var trough = 0f

    private val init = 2
    private var nowStatus = init
    private var lastStatus = init

    init {
        model = WAIT_MODEL
        initCount = 0
    }

    private fun setModel() {
        this.model = 1
    }

    fun updateStep(x: Float, y: Float, z: Float) {

        val curTime = System.currentTimeMillis()
        if (lastStepTime == 0L) {
            lastStepTime = curTime
        }
        val DELAY = 100
        if (curTime - lastStepTime < DELAY)
            return
        lastStepTime = curTime

        val b = BigDecimal(Math.sqrt((x * x + y * y + z * z).toDouble()))
        val vel = b.setScale(2, BigDecimal.ROUND_DOWN).toFloat()

        if (lastVel == 0f)
            lastVel = vel

        val MAX_VEL = 60f
        val MIN_VEL = 10f
        if (vel < MIN_VEL || vel > MAX_VEL) {
            initStepDetector()
            return
        }
        val velThreshold = getvelThreshold()

        if (vel > lastVel) {
            val up = 0
            if (lastStatus == up || crest == 0f)
                crest = vel
            if (trough == 0f)
                trough = lastVel
            nowStatus = up
        } else if (vel <= lastVel) {
            val down = 1
            if (lastStatus == down || trough == 0f)
                trough = vel
            if (crest == 0f)
                crest = lastVel
            nowStatus = down
        }

        if (nowStatus != lastStatus && nowStatus != init && lastStatus != init) {
            if (crest - trough >= velThreshold) {
                realSteps()
                updatevelThreshold(crest - trough)
            } else {
                initStepDetector()
            }
            trough = 0f
            crest = trough
        }

        lastVel = vel
        lastStatus = nowStatus
    }

    private fun realSteps() {
        initCount = 0
        val activityModel = 1
        if (model == activityModel || model == RUN_MODEL)
            stepListener.step(1.toLong())
        else {
            val WAIT_STEPS: Long = 60
            if (tempSteps >= WAIT_STEPS) {
                model = RUN_MODEL
                stepListener.step(1.toLong() + tempSteps)
                tempSteps = 0
            } else
                tempSteps++
        }
    }

    fun updateModel() {
        setModel()
    }

    private fun initThreshold() {
        pos = 0
        for (i in 0..14)
            fourVels[i] = initVel

    }

    private fun initStepDetector() {
        if (initCount < 2) {
            initCount++
            return
        }
        nowStatus = init
        lastStatus = init
        crest = 0f
        trough = 0f
        initThreshold()
        if (model == RUN_MODEL)
            model = WAIT_MODEL
        tempSteps = 0
        initCount = 0

    }

    private fun updatevelThreshold(vel: Float) {
        fourVels[pos++] = vel
        if (pos == 15)
            pos = 0
    }

    private fun getvelThreshold(): Float {
        var sum = 0f
        for (i in 0..14) {
            sum += fourVels[i]
        }
        sum = sum / 15
        return sum
    }
}