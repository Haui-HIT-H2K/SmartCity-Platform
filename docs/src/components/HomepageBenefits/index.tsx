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

type BenefitItem = {
  title: string
  description: string
  icon: string
}


const BenefitsList: BenefitItem[] = [
  {
    title: "Tính Sẵn sàng Cao (HA)",
    description:
      "Kiến trúc đệm (Edge Storage) và PULL model độc đáo giúp hệ thống không bao giờ bị sập do quá tải dữ liệu (ingestion overhead) từ sensor.",
    icon: "🛡️",
  },
  {
    title: "Ưu tiên Dữ liệu Thông minh",
    description:
      "Tự động phân luồng và ưu tiên xử lý Nóng/Ấm/Lạnh. Đảm bảo dữ liệu cảnh báo (Hot) được xử lý tức thì, tối ưu hóa tài nguyên.",
    icon: "⚡",
  },
  {
    title: "Tuân thủ Chuẩn Quốc tế",
    description:
      "Nền tảng tuân thủ 100% chuẩn NGSI-LD và FIWARE Data Models. Sẵn sàng tích hợp và liên thông dữ liệu với các hệ thống khác.",
    icon: "🌐", 
  },
  {
    title: "Mở rộng Linh hoạt",
    description:
      "Kiến trúc Microservice (đóng gói Docker) cho phép nâng cấp và mở rộng từng thành phần (Agent, Broker, Storage) một cách độc lập.",
    icon: "📈",
  },
]


function Benefit({ title, description, icon }: BenefitItem) {
  return (
    <div className={clsx("col col--6", styles.benefitCol)}>
      <div className={styles.benefitCard}>
        <div className={styles.benefitIcon}>{icon}</div>
        <Heading as="h3" className={styles.benefitTitle}>
          {title}
        </Heading>
        <p className={styles.benefitDescription}>{description}</p>
      </div>
    </div>
  )
}

export default function HomepageBenefits(): ReactNode {
  return (
    <section className={styles.benefits}>
      <div className="container">
        <div className={styles.benefitsHeader}>
          <Heading as="h2" className={styles.benefitsTitle}>
            Tại sao chọn SmartCity-Platform?
          </Heading>
          <p className={styles.benefitsSubtitle}>
            Nền tảng dữ liệu đô thị vững chắc, sẵn sàng cho tương lai.
          </p>
        </div>
        <div className="row">
          {BenefitsList.map((props, idx) => (
            <Benefit key={idx} {...props} />
          ))}
        </div>
      </div>
    </section>
  )
}