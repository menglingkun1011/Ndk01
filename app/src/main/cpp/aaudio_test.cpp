/**
 * aaudio Callback模式录音播放Demo
 * 基于Android 11 (API 30)
 *
 * 使用aaudio的callback模式：录音数据通过回调函数获取，
 * 然后直接转发给播放流
 */

#include <aaudio/AAudio.h>
#include <android/log.h>
#include <thread>
#include <atomic>
#include <mutex>
#include <vector>

#define LOG_TAG "AAudioCallbackDemo"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

static std::atomic<bool> g_running{true};
static std::mutex g_mutex;
static std::vector<float> g_buffer;
static const size_t kBufferSize = 480;  // 10ms @ 48kHz

// 音频参数
static constexpr int32_t kSampleRate = 48000;
static constexpr int32_t kChannels = 1;
static constexpr int32_t kFramesPerBuffer = 480;

// Callback函数：处理录音数据
aaudio_data_callback_result_t inputCallback(
    AAudioStream* stream,
    void* userData,
    void* audioData,
    int32_t numFrames) {

    (void)stream;
    (void)userData;

    // 获取录音数据
    float* inputBuffer = static_cast<float*>(audioData);

    // 将数据保存到共享缓冲区
    {
        std::lock_guard<std::mutex> lock(g_mutex);
        const float* src = inputBuffer;
        for (int32_t i = 0; i < numFrames; i++) {
            g_buffer.push_back(src[i]);
        }
    }

    return AAUDIO_CALLBACK_RESULT_CONTINUE;
}

// Callback函数：播放数据
aaudio_data_callback_result_t outputCallback(
    AAudioStream* stream,
    void* userData,
    void* audioData,
    int32_t numFrames) {

    (void)userData;

    float* outputBuffer = static_cast<float*>(audioData);

    // 从共享缓冲区读取数据进行播放
    {
        std::lock_guard<std::mutex> lock(g_mutex);
        size_t available = g_buffer.size();
        int32_t framesToPlay = (available > (size_t)numFrames) ? numFrames : (int32_t)available;

        for (int32_t i = 0; i < framesToPlay; i++) {
            outputBuffer[i] = g_buffer.front();
            g_buffer.erase(g_buffer.begin());
        }

        // 填充剩余空间为0
        for (int32_t i = framesToPlay; i < numFrames; i++) {
            outputBuffer[i] = 0.0f;
        }
    }

    if (!g_running.load()) {
        return AAUDIO_CALLBACK_RESULT_STOP;
    }

    return AAUDIO_CALLBACK_RESULT_CONTINUE;
}

int main() {
    LOGI("AAudio Callback Demo Start");

    AAudioStream* inputStream = nullptr;
    AAudioStream* outputStream = nullptr;

    // 创建录音流(输入流)
    AAudioStreamBuilder* inputBuilder = nullptr;
    aaudio_result_t result = AAudio_createStreamBuilder(&inputBuilder);
    if (result != AAUDIO_OK) {
        LOGE("Failed to create input stream builder: %s", AAudio_convertResultToText(result));
        return -1;
    }

    AAudioStreamBuilder_setDirection(inputBuilder, AAUDIO_DIRECTION_INPUT);
    AAudioStreamBuilder_setSampleRate(inputBuilder, kSampleRate);
    AAudioStreamBuilder_setChannelCount(inputBuilder, kChannels);
    AAudioStreamBuilder_setFormat(inputBuilder, AAUDIO_FORMAT_PCM_FLOAT);
    AAudioStreamBuilder_setPerformanceMode(inputBuilder, AAUDIO_PERFORMANCE_MODE_LOW_LATENCY);
    AAudioStreamBuilder_setSharingMode(inputBuilder, AAUDIO_SHARING_MODE_EXCLUSIVE);
    AAudioStreamBuilder_setDataCallback(inputBuilder, inputCallback, nullptr);

    result = AAudioStreamBuilder_openStream(inputBuilder, &inputStream);
    AAudioStreamBuilder_delete(inputBuilder);

    if (result != AAUDIO_OK) {
        LOGE("Failed to open input stream: %s", AAudio_convertResultToText(result));
        return -1;
    }

    LOGI("Input stream created: sampleRate=%d, channels=%d",
         AAudioStream_getSampleRate(inputStream),
         AAudioStream_getChannelCount(inputStream));

    // 创建播放流(输出流)
    AAudioStreamBuilder* outputBuilder = nullptr;
    result = AAudio_createStreamBuilder(&outputBuilder);
    if (result != AAUDIO_OK) {
        LOGE("Failed to create output stream builder: %s", AAudio_convertResultToText(result));
        AAudioStream_close(inputStream);
        return -1;
    }

    AAudioStreamBuilder_setDirection(outputBuilder, AAUDIO_DIRECTION_OUTPUT);
    AAudioStreamBuilder_setSampleRate(outputBuilder, kSampleRate);
    AAudioStreamBuilder_setChannelCount(outputBuilder, kChannels);
    AAudioStreamBuilder_setFormat(outputBuilder, AAUDIO_FORMAT_PCM_FLOAT);
    AAudioStreamBuilder_setPerformanceMode(outputBuilder, AAUDIO_PERFORMANCE_MODE_LOW_LATENCY);
    AAudioStreamBuilder_setSharingMode(outputBuilder, AAUDIO_SHARING_MODE_EXCLUSIVE);
    AAudioStreamBuilder_setDataCallback(outputBuilder, outputCallback, nullptr);
    AAudioStreamBuilder_setFramesPerDataCallback(outputBuilder, kFramesPerBuffer);

    result = AAudioStreamBuilder_openStream(outputBuilder, &outputStream);
    AAudioStreamBuilder_delete(outputBuilder);

    if (result != AAUDIO_OK) {
        LOGE("Failed to open output stream: %s", AAudio_convertResultToText(result));
        AAudioStream_close(inputStream);
        return -1;
    }

    LOGI("Output stream created: sampleRate=%d, channels=%d",
         AAudioStream_getSampleRate(outputStream),
         AAudioStream_getChannelCount(outputStream));

    // 启动流
    result = AAudioStream_requestStart(inputStream);
    if (result != AAUDIO_OK) {
        LOGE("Failed to start input stream: %s", AAudio_convertResultToText(result));
        AAudioStream_close(inputStream);
        AAudioStream_close(outputStream);
        return -1;
    }

    result = AAudioStream_requestStart(outputStream);
    if (result != AAUDIO_OK) {
        LOGE("Failed to start output stream: %s", AAudio_convertResultToText(result));
        AAudioStream_requestStop(inputStream);
        AAudioStream_close(inputStream);
        AAudioStream_close(outputStream);
        return -1;
    }

    LOGI("Recording and playing... Press Enter to stop.");

    // 等待用户输入(在Android上可以通过其他方式触发)
    // 这里使用线程 sleep 模拟，实际使用时可以通过信号量等方式
    std::this_thread::sleep_for(std::chrono::seconds(10));

    LOGI("Stopping...");

    // 停止并关闭流
    g_running.store(false);

    AAudioStream_requestStop(inputStream);
    AAudioStream_requestStop(outputStream);

    // 等待一下让callback完成
    std::this_thread::sleep_for(std::chrono::milliseconds(100));

    AAudioStream_close(inputStream);
    AAudioStream_close(outputStream);

    LOGI("AAudio Callback Demo End");

    return 0;
}
