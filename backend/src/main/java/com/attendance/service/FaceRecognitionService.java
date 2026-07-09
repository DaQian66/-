package com.attendance.service;

import com.attendance.entity.Employee;
import com.attendance.repository.EmployeeRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.opencv.opencv_java;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * 人脸识别服务 - 基于 OpenCV
 * 使用 Haar Cascade 进行人脸检测，直方图比对进行人脸匹配
 */
@Service
public class FaceRecognitionService {

    private static final Logger log = LoggerFactory.getLogger(FaceRecognitionService.class);

    @Autowired
    private EmployeeRepository employeeRepository;

    @Value("${app.upload.path:./uploads}")
    private String uploadPath;

    @Value("${app.face.recognition.threshold:70.0}")
    private double confidenceThreshold;

    /** Haar Cascade 分类器，用于人脸检测 */
    private CascadeClassifier faceDetector;

    /** 人脸比对标准尺寸 */
    private static final int FACE_WIDTH = 200;
    private static final int FACE_HEIGHT = 200;

    /** 分区域比对的网格划分 */
    private static final int GRID_ROWS = 5;
    private static final int GRID_COLS = 5;

    /**
     * 初始化 OpenCV 原生库，加载人脸检测模型
     */
    @PostConstruct
    public void init() {
        try {
            // 加载 OpenCV 原生库
            Loader.load(opencv_java.class);
            log.info("OpenCV native library loaded successfully");

            // 从 classpath 加载 Haar Cascade XML，写入临时文件
            InputStream cascadeStream = getClass().getClassLoader()
                    .getResourceAsStream("opencv/haarcascade_frontalface_alt.xml");
            if (cascadeStream == null) {
                throw new RuntimeException("找不到人脸检测模型文件: opencv/haarcascade_frontalface_alt.xml");
            }

            File tempCascade = File.createTempFile("haarcascade_frontalface_alt", ".xml");
            tempCascade.deleteOnExit();
            try (FileOutputStream fos = new FileOutputStream(tempCascade)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = cascadeStream.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
            }
            cascadeStream.close();

            faceDetector = new CascadeClassifier(tempCascade.getAbsolutePath());
            if (faceDetector.empty()) {
                throw new RuntimeException("人脸检测模型加载失败");
            }
            log.info("Haar Cascade face detector loaded successfully, temp file: {}", tempCascade.getAbsolutePath());
        } catch (Exception e) {
            log.error("FaceRecognitionService 初始化失败: {}", e.getMessage(), e);
            throw new RuntimeException("人脸识别服务初始化失败: " + e.getMessage(), e);
        }
    }

    /**
     * 验证人脸是否匹配
     * @param employeeId 员工ID
     * @param base64ImageData 拍摄的人脸照片（base64编码的JPEG数据）
     * @return 匹配结果
     */
    public FaceMatchResult verifyFace(Integer employeeId, String base64ImageData) {
        // 1. 校验参数
        if (base64ImageData == null || base64ImageData.isEmpty()) {
            return new FaceMatchResult(false, 0.0, "未收到人脸图像数据，请重新拍摄");
        }

        // 2. 获取员工信息
        Employee emp = employeeRepository.findById(employeeId).orElse(null);
        if (emp == null) {
            return new FaceMatchResult(false, 0.0, "员工不存在");
        }

        // 3. 检查是否注册了人脸照片
        if (emp.getFaceImage() == null || emp.getFaceImage().isEmpty()) {
            return new FaceMatchResult(false, 0.0, "您尚未注册人脸信息，请联系管理员上传人脸照片");
        }

        // 4. 解码拍摄照片的base64数据
        Mat capturedImage;
        try {
            capturedImage = base64ToMat(base64ImageData);
        } catch (Exception e) {
            log.warn("base64解码失败: {}", e.getMessage());
            return new FaceMatchResult(false, 0.0, "人脸图像数据格式错误，请重新拍照");
        }

        // 5. 检测拍摄照片中的人脸
        Mat capturedFace = null;
        try {
            capturedFace = detectFace(capturedImage);
        } finally {
            capturedImage.release();
        }

        if (capturedFace == null) {
            return new FaceMatchResult(false, 0.0, "未检测到人脸，请正对摄像头并确保光线充足");
        }

        // 6. 加载注册的人脸照片
        String faceImagePath = emp.getFaceImage();
        // 规范化路径：getAbsoluteFile() 会正确解析 ./ 等相对路径
        String uploadAbsolutePath = new File(uploadPath).getAbsoluteFile().getPath();
        String relativePath = faceImagePath.replace("/uploads/", "");
        File registeredFile = new File(uploadAbsolutePath, relativePath);
        log.info("加载注册人脸: uploadPath={}, absolutePath={}, faceImage={}, finalPath={}",
                uploadPath, uploadAbsolutePath, faceImagePath, registeredFile.getAbsolutePath());
        if (!registeredFile.exists()) {
            log.warn("注册人脸照片文件不存在: {}", registeredFile.getAbsolutePath());
            capturedFace.release();
            return new FaceMatchResult(false, 0.0, "注册人脸照片文件丢失，请联系管理员重新上传");
        }

        // 用 Java NIO 读取文件字节（支持中文路径），再用 OpenCV imdecode 解码
        Mat registeredImage;
        try {
            byte[] fileBytes = Files.readAllBytes(registeredFile.toPath());
            registeredImage = Imgcodecs.imdecode(new MatOfByte(fileBytes), Imgcodecs.IMREAD_COLOR);
        } catch (IOException e) {
            log.warn("读取注册人脸照片失败: {} - {}", registeredFile.getAbsolutePath(), e.getMessage());
            capturedFace.release();
            return new FaceMatchResult(false, 0.0, "无法读取注册人脸照片: " + e.getMessage());
        }
        if (registeredImage.empty()) {
            log.warn("OpenCV 无法解码注册人脸照片: {}", registeredFile.getAbsolutePath());
            capturedFace.release();
            return new FaceMatchResult(false, 0.0, "无法解码注册人脸照片，请检查照片格式是否为JPG/PNG");
        }

        // 7. 检测注册照片中的人脸
        Mat registeredFace = null;
        try {
            registeredFace = detectFace(registeredImage);
        } finally {
            registeredImage.release();
        }

        if (registeredFace == null) {
            capturedFace.release();
            return new FaceMatchResult(false, 0.0, "注册人脸照片中未检测到人脸，请联系管理员重新上传");
        }

        // 8. 预处理两张人脸，进行直方图比对
        double similarityScore;
        try {
            Mat processedCaptured = preprocessFace(capturedFace);
            Mat processedRegistered = preprocessFace(registeredFace);
            similarityScore = compareFaces(processedCaptured, processedRegistered);
            processedCaptured.release();
            processedRegistered.release();
        } finally {
            capturedFace.release();
            registeredFace.release();
        }

        // 9. 转换为百分比置信度
        double confidence = similarityScore * 100.0;

        // 10. 判断是否通过
        boolean matched = confidence >= confidenceThreshold;

        log.info("人脸比对结果: employeeId={}, similarity={}%, threshold={}%, matched={}",
                employeeId, Math.round(confidence), Math.round(confidenceThreshold), matched);

        String message;
        if (matched) {
            message = String.format("人脸识别成功，置信度: %.0f%%", confidence);
        } else {
            message = String.format("人脸验证未通过，相似度仅 %.0f%%，请重新拍照", confidence);
        }

        return new FaceMatchResult(matched, confidence, message);
    }

    /**
     * 从图像中检测人脸，返回最大的人脸区域
     * @param image 输入图像 (BGR)
     * @return 人脸ROI区域 (灰度图)，无人脸返回null
     */
    public Mat detectFace(Mat image) {
        // 转换为灰度图进行检测
        Mat gray = new Mat();
        Imgproc.cvtColor(image, gray, Imgproc.COLOR_BGR2GRAY);

        // 直方图均衡化增强对比度
        Imgproc.equalizeHist(gray, gray);

        // 检测人脸
        MatOfRect faces = new MatOfRect();
        faceDetector.detectMultiScale(gray, faces, 1.1, 3,
                org.opencv.objdetect.Objdetect.CASCADE_SCALE_IMAGE,
                new org.opencv.core.Size(80, 80),
                new org.opencv.core.Size());

        Rect[] facesArray = faces.toArray();
        gray.release();

        if (facesArray.length == 0) {
            return null;
        }

        // 返回面积最大的人脸
        Rect largestFace = facesArray[0];
        double maxArea = largestFace.area();
        for (int i = 1; i < facesArray.length; i++) {
            double area = facesArray[i].area();
            if (area > maxArea) {
                maxArea = area;
                largestFace = facesArray[i];
            }
        }

        // 裁剪人脸ROI并转为灰度返回
        Mat faceROI = new Mat(image, largestFace);
        Mat grayFace = new Mat();
        Imgproc.cvtColor(faceROI, grayFace, Imgproc.COLOR_BGR2GRAY);
        return grayFace;
    }

    /**
     * 人脸预处理：灰度化 + 直方图均衡化 + 统一尺寸
     */
    public Mat preprocessFace(Mat faceROI) {
        Mat result = faceROI.clone();
        // 直方图均衡化应对光照变化
        Imgproc.equalizeHist(result, result);
        // 缩放到统一尺寸
        Imgproc.resize(result, result, new Size(FACE_WIDTH, FACE_HEIGHT));
        return result;
    }

    /**
     * 使用分区域直方图比对两张人脸
     * 将人脸划分为 5×5 网格，每个格子单独比对直方图并取平均。
     * 这比全局直方图更能捕捉人脸各区域的局部差异（眼睛/鼻子/嘴巴等）。
     *
     * 同时综合灰度直方图 + 梯度方向直方图，提高辨别力。
     *
     * @param face1 预处理后的人脸1
     * @param face2 预处理后的人脸2
     * @return 相似度分数 0.0 ~ 1.0（越高越相似）
     */
    public double compareFaces(Mat face1, Mat face2) {
        int cellW = FACE_WIDTH / GRID_COLS;
        int cellH = FACE_HEIGHT / GRID_ROWS;

        // 计算梯度图用于纹理比对
        Mat grad1 = computeGradientMagnitude(face1);
        Mat grad2 = computeGradientMagnitude(face2);

        double totalIntensityScore = 0.0;
        double totalGradientScore = 0.0;
        int validCells = 0;

        for (int r = 0; r < GRID_ROWS; r++) {
            for (int c = 0; c < GRID_COLS; c++) {
                int x = c * cellW;
                int y = r * cellH;
                int w = Math.min(cellW, FACE_WIDTH - x);
                int h = Math.min(cellH, FACE_HEIGHT - y);

                // 灰度直方图比对
                Mat cell1 = new Mat(face1, new Rect(x, y, w, h));
                Mat cell2 = new Mat(face2, new Rect(x, y, w, h));
                totalIntensityScore += compareCellHistograms(cell1, cell2);
                cell1.release();
                cell2.release();

                // 梯度直方图比对
                Mat gcell1 = new Mat(grad1, new Rect(x, y, w, h));
                Mat gcell2 = new Mat(grad2, new Rect(x, y, w, h));
                totalGradientScore += compareCellHistograms(gcell1, gcell2);
                gcell1.release();
                gcell2.release();

                validCells++;
            }
        }

        grad1.release();
        grad2.release();

        if (validCells == 0) return 0.0;
        // 灰度直方图权重 0.6 + 梯度直方图权重 0.4
        double avgIntensity = totalIntensityScore / validCells;
        double avgGradient = totalGradientScore / validCells;
        double combinedScore = avgIntensity * 0.6 + avgGradient * 0.4;

        return Math.max(0.0, combinedScore);
    }

    /**
     * 计算梯度幅值图 — 提取边缘/纹理信息，对不同人脸结构更敏感
     */
    private Mat computeGradientMagnitude(Mat gray) {
        Mat gradX = new Mat();
        Mat gradY = new Mat();
        Imgproc.Sobel(gray, gradX, CvType.CV_32F, 1, 0);
        Imgproc.Sobel(gray, gradY, CvType.CV_32F, 0, 1);

        Mat magnitude = new Mat();
        Core.magnitude(gradX, gradY, magnitude);

        // 归一化到 0-255
        Core.normalize(magnitude, magnitude, 0, 255, Core.NORM_MINMAX);
        magnitude.convertTo(magnitude, CvType.CV_8UC1);

        gradX.release();
        gradY.release();
        return magnitude;
    }

    /**
     * 计算两个区域图像的直方图相关性
     */
    private double compareCellHistograms(Mat cell1, Mat cell2) {
        Mat hist1 = computeHistogram(cell1);
        Mat hist2 = computeHistogram(cell2);
        double correlation = Imgproc.compareHist(hist1, hist2, Imgproc.HISTCMP_CORREL);
        hist1.release();
        hist2.release();
        return Math.max(0.0, correlation);
    }

    /**
     * 计算图像的256级直方图
     */
    private Mat computeHistogram(Mat grayImage) {
        MatOfInt histSize = new MatOfInt(256);
        MatOfFloat histRange = new MatOfFloat(0, 256);
        Mat hist = new Mat();

        List<Mat> images = new ArrayList<>();
        images.add(grayImage);

        Imgproc.calcHist(images, new MatOfInt(0), new Mat(), hist, histSize, histRange);
        Core.normalize(hist, hist, 0, 1, Core.NORM_MINMAX);
        return hist;
    }

    /**
     * 将 Base64 编码的图像数据解码为 OpenCV Mat (BGR)
     */
    public Mat base64ToMat(String base64Data) throws IOException {
        // 去掉可能的 data:image/...;base64, 前缀
        if (base64Data.contains(",")) {
            base64Data = base64Data.substring(base64Data.indexOf(",") + 1);
        }

        byte[] imageBytes = Base64.getDecoder().decode(base64Data);

        // 直接从字节数组解码为OpenCV Mat，避免BufferedImage中间转换的质量损失
        Mat mat = Imgcodecs.imdecode(new MatOfByte(imageBytes), Imgcodecs.IMREAD_COLOR);
        if (mat.empty()) {
            throw new IOException("无法解码图像数据，请确认照片格式正确");
        }
        return mat;
    }

    /**
     * 人脸匹配结果
     */
    @Data
    @AllArgsConstructor
    public static class FaceMatchResult {
        /** 是否匹配成功 */
        private boolean matched;
        /** 置信度 0-100 */
        private double confidence;
        /** 结果消息 */
        private String message;
    }
}