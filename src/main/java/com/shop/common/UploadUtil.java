package com.shop.common;

import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * 商品图上传：保存到 webapp 下的 upload 目录，返回可访问的相对URL
 */
public final class UploadUtil {

    private static final String[] ALLOWED_EXT = {"jpg", "jpeg", "png", "gif", "webp"};

    private UploadUtil() {
    }

    /**
     * @return 例如 /upload/202609/ab12cd34.jpg
     */
    public static String saveImage(MultipartFile file, HttpServletRequest request, String uploadDir) {
        if (file == null || file.isEmpty()) {
            throw new BizException("请选择图片文件");
        }
        String original = file.getOriginalFilename();
        if (original == null || !original.contains(".")) {
            throw new BizException("文件名不合法");
        }
        String ext = original.substring(original.lastIndexOf('.') + 1).toLowerCase();
        boolean allowed = false;
        for (String e : ALLOWED_EXT) {
            if (e.equals(ext)) {
                allowed = true;
                break;
            }
        }
        if (!allowed) {
            throw new BizException("仅支持 jpg/png/gif/webp 格式图片");
        }

        String month = new SimpleDateFormat("yyyyMM").format(new Date());
        String name = UUID.randomUUID().toString().replace("-", "").substring(0, 16) + "." + ext;

        // 真实保存目录 = 应用根目录/uploadDir/yyyyMM/
        String realRoot = request.getServletContext().getRealPath("/");
        File dir = new File(realRoot, uploadDir + "/" + month);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new BizException("上传目录创建失败");
        }
        try {
            file.transferTo(new File(dir, name));
        } catch (IOException e) {
            throw new BizException("图片保存失败");
        }
        return "/" + uploadDir + "/" + month + "/" + name;
    }
}
