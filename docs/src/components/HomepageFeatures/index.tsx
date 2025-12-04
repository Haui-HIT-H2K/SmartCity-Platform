/** 

 * Copyright 2025 Haui.HIT - H2K

 *

 * Licensed under the Apache License, Version 2.0 (the "License");

 * http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software

 * distributed under the License is distributed on an "AS IS" BASIS,

 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.

 */

import type { ReactNode } from "react"
import clsx from "clsx"
import Heading from "@theme/Heading"
import styles from "./styles.module.css"

type FeatureItem = {
  title: string
  icon: string
  description: ReactNode
}


const FeatureList: FeatureItem[] = [
  {
    title: "Kiến trúc Đệm & PULL",
    icon: "🛡️",
    description: (
      <>
        Sử dụng mô hình PULL từ Edge Storage (buffer) để chống quá tải, đảm bảo
        hệ thống luôn sẵn sàng (High Availability).
      </>
    ),
  },
  {
    title: "Xử lý Ưu tiên Thông minh",
    icon: "⚡",
    description: (
      <>
        Smart Agent tự động phân loại và ưu tiên xử lý dữ liệu Nóng (tức thì), Ấm
        (hàng ngày) và Lạnh (lưu trữ).
      </>
    ),
  },
  {
    title: "Chuẩn hóa NGSI-LD",
    icon: "📊",
    description: (
      <>
        Tuân thủ 100% chuẩn NGSI-LD và FIWARE Data Models, sẵn sàng cho liên
        thông dữ liệu đô thị thông minh.
      </>
    ),
  },
  {
    title: "Kiến trúc Microservices",
    icon: "🏗️",
    description: (
      <>
        Đóng gói bằng Docker Compose, cho phép triển khai, bảo trì và mở rộng
        từng dịch vụ độc lập.
      </>
    ),
  },
  {
    title: "API Dữ liệu Đa tầng",
    icon: "🔗",
    description: (
      <>
        Cung cấp API thời gian thực (Lớp Nóng) và API lịch sử (Lớp Ấm) cho mọi
        nhu cầu ứng dụng.
      </>
    ),
  },
  {
    title: "Mã nguồn mở (FOSS)",
    icon: "🌐",
    description: (
      <>
        Phát triển theo giấy phép Apache 2.0, tuân thủ tiêu chí FOSS và sẵn
        sàng cho cộng đồng đóng góp.
      </>
    ),
  },
]


function Feature({ title, icon, description }: FeatureItem) {
  return (
    <div className={clsx("col col--4", styles.featureCol)}>
      <div className={styles.featureCard}>
        <div className={styles.featureIcon}>{icon}</div>
        <Heading as="h3" className={styles.featureTitle}>
          {title}
        </Heading>
        <p className={styles.featureDescription}>{description}</p>
      </div>
    </div>
  )
}

export default function HomepageFeatures(): ReactNode {
  return (
    <section className={styles.features}>
      <div className="container">
        <div className={styles.featuresHeader}>
          <Heading as="h2" className={styles.featuresTitle}>
            Tính năng chính
          </Heading>
          <p className={styles.featuresSubtitle}>
            SmartCity-Platform cung cấp một nền tảng dữ liệu vững chắc và linh
            hoạt.
          </p>
        </div>
        <div className="row">
          {FeatureList.map((props, idx) => (
            <Feature key={idx} {...props} />
          ))}
        </div>
      </div>
    </section>
  )
}