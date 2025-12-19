# 静态图片准备指南

为了让种子数据里的示例图片直接可用，请在构建镜像前将对应文件放入 `backend/src/main/resources/uploads/`，文件名与种子数据保持一致。后端 Dockerfile 会在最终镜像中将该目录拷贝到 `/app/uploads`，Spring 会通过 `/api/uploads/**` 提供访问。

## 需要的文件名

- `campus_cap.jpg`
- `thermos_black.jpg`
- `keychain_blue.jpg`
- `airpods_case.jpg`
- `hoodie_gray.jpg`
- `banner_hall.jpg`
- `banner_umbrella.jpg`
- `banner_toplist.jpg`

## 建议的尺寸与内容

- 物品类（帽子/保温杯/钥匙串/耳机盒/连帽衫）：横向或正方形均可，建议 800px 以上。
- Banner 类：16:5 或 3:1 横图，建议 1200px 以上，内容清晰，少文字。

## 放置方法

1. 将上述文件复制到 `backend/src/main/resources/uploads/`（与 `.gitkeep` 同级）。
2. 重新构建镜像（示例）：  
   `docker-compose -f docker-compose.prod.yml build backend frontend`
3. 重新启动服务：  
   `docker-compose -f docker-compose.prod.yml up -d`

## 自定义

- 如需更换文件名，请同步修改 `backend/src/main/resources/data.sql` 中的 `image_url` 和 `media_files` 记录。
- 如果不想在镜像内内置图片，也可以把 `image_url` 改成公网可访问的 URL。
