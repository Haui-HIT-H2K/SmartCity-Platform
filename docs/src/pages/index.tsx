SPDX-License-Identifier: Apache-2.0
/** 

 * Copyright 2025 Haui.HIT - H2K

 *

 * Licensed under the Apache License, Version 2.0 (the "License");

 * http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software

 * distributed under the License is distributed on an "AS IS" BASIS,

 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.

 */

import React from 'react'
import clsx from 'clsx'
import Link from '@docusaurus/Link'
import useDocusaurusContext from '@docusaurus/useDocusaurusContext'
import Layout from '@theme/Layout'
import HomepageFeatures from '@site/src/components/HomepageFeatures'
import HomepageBenefits from '@site/src/components/HomepageBenefits'

import styles from './index.module.css'

const HeroSplitSection: React.FC = () => {
  return (
    <section className={styles.splitSection}>
      <div className={styles.techBg}></div>
      <div className="container">
        <div className={styles.splitInner}>
          <div className={styles.splitLeft}>
            <h2 className={styles.splitHeading}>
              Hạ tầng liên kết Dữ liệu Đô thị
            </h2>
            <p className={styles.splitText}>
              SmartCity-Platform là nền tảng Smart City tuân thủ NGSI-LD, sử
              dụng kiến trúc PULL model và ưu tiên dữ liệu (Nóng/Ấm/Lạnh) để đảm
              bảo tính sẵn sàng cao (HA) và chống quá tải.
            </p>
            <div className={styles.splitButtons}>
              <Link className={clsx('button', styles.smallPrimary)} to="/overview/intro">
                Khám phá ngay
              </Link>
            </div>
            <div className={styles.statsRow}>
              <div className={styles.statItem}>
                <div className={styles.statNumber}>100%</div>
                <div className={styles.statLabel}>Mã nguồn mở</div>
              </div>
              <div className={styles.statItem}>
                <div className={styles.statNumber}>NGSI-LD</div>
                <div className={styles.statLabel}>Tuân thủ Chuẩn</div>
              </div>
              <div className={styles.statItem}>
                <div className={styles.statNumber}>HA</div>
                <div className={styles.statLabel}>Chống Quá tải</div>
              </div>
            </div>
          </div>

          <div className={styles.splitRight}>
            <div className={styles.codeCard}>
              <div className={styles.cardHeader}>
                <div className={styles.circles}>
                  <span></span>
                  <span></span>
                  <span></span>
                </div>
                <div className={styles.cardTitle}>Terminal</div>
              </div>
              <div className={styles.cardBody}>
                <div className={styles.promptLine}>
                  <span className={styles.prompt}>$</span>
                  <span className={styles.command}>
                    {"curl -X GET 'http://localhost:1026/ngsi-ld/v1/entities?type=AirQualityObserved&limit=1'"}
                  </span>
                </div>
                <pre className={styles.response}>
                  {`[
  {
    "id": "urn:ngsi-ld:AirQualityObserved:sensor-01",
    "type": "AirQualityObserved",
    "aqi": {
      "type": "Property",
      "value": 152
    },
    "location": {
      "type": "GeoProperty",
      "value": { "type": "Point", "coordinates": [105.8, 21.0] }
    },
    "@context": [
      "https://smartdatamodels.org/context.jsonld"
    ]
  }
]`}
                </pre>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  )
}

const Home: React.FC = () => {
  const { siteConfig } = useDocusaurusContext()
  return (
    <Layout
      title={`${siteConfig.title} - Nền tảng Smart City (PMNM 2025)`}
      description="SmartCity-Platform: Nền tảng dữ liệu đô thị (PMNM 2025) với kiến trúc PULL model và ưu tiên dữ liệu (Nóng/Ấm/Lạnh) tuân thủ NGSI-LD."
    >
      <div className={styles.pageWrapper}>
        <main>
          <HeroSplitSection />
          <HomepageFeatures />
          <HomepageBenefits />
        </main>
      </div>
    </Layout>
  )
}

export default Home