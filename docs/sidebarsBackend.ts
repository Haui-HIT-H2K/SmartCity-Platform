
import type { SidebarsConfig } from "@docusaurus/plugin-content-docs";

const sidebars: SidebarsConfig = {
  backendSidebar: [
    {
      type: "doc",
      id: "intro",
      label: "Giới thiệu",
    },
    {
      type: "doc",
      id: "api-reference",
      label: "API Reference",
    },
    {
      type: "doc",
      id: "data-model",
      label: "Data Model",
    },
    {
      type: "doc",
      id: "configuration",
      label: "Configuration",
    },
  ],
};

export default sidebars;
