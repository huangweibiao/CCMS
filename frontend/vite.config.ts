import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'
import { createHtmlPlugin } from 'vite-plugin-html'
import { visualizer } from 'rollup-plugin-visualizer'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  
  const isProduction = mode === 'production'
  const isDevelopment = mode === 'development'
  const isAnalyze = mode === 'analyze'
  
  const plugins = [
    vue(),
    createHtmlPlugin({
      minify: isProduction,
      inject: {
        data: {
          title: env.VITE_APP_TITLE || '企业级费控管理系统',
          description: env.VITE_APP_DESCRIPTION || 'Corporate Cost Management System'
        }
      }
    })
  ]
  
  // 生产环境添加包分析插件
  if (isAnalyze) {
    plugins.push(
      visualizer({
        filename: './dist/bundle-analysis.html',
        open: true,
        gzipSize: true,
        brotliSize: true
      })
    )
  }
  
  return {
    plugins,
    
    // 基础路径配置
    base: isProduction ? '/ccms/' : '/',
    
    // 路径别名配置
    resolve: {
      alias: {
        '@': resolve(__dirname, 'src'),
        '@components': resolve(__dirname, 'src/components'),
        '@views': resolve(__dirname, 'src/views'),
        '@utils': resolve(__dirname, 'src/utils'),
        '@api': resolve(__dirname, 'src/api'),
        '@stores': resolve(__dirname, 'src/stores')
      }
    },
    
    // 开发服务器配置
    server: {
      port: parseInt(env.VITE_PORT) || 3000,
      host: env.VITE_HOST || 'localhost',
      open: isDevelopment,
      cors: true,
      proxy: {
        '/api': {
          target: env.VITE_API_BASE_URL || 'http://localhost:8080',
          changeOrigin: true,
          secure: false,
          rewrite: (path) => path.replace(/^\/api/, '')
        }
      }
    },
    
    // 预览服务器配置
    preview: {
      port: 4173,
      host: 'localhost',
      cors: true
    },
    
    // 构建配置
    build: {
      outDir: 'dist',
      assetsDir: 'assets',
      sourcemap: isProduction ? 'hidden' : false,
      minify: isProduction ? 'terser' : false,
      terserOptions: {
        compress: {
          drop_console: isProduction,
          drop_debugger: isProduction
        }
      },
      rollupOptions: {
        output: {
          chunkFileNames: 'js/[name]-[hash].js',
          entryFileNames: 'js/[name]-[hash].js',
          assetFileNames: (assetInfo) => {
            const extType = assetInfo.name.split('.').pop()
            if (/png|jpe?g|svg|gif|tiff|bmp|ico/i.test(extType)) {
              return 'img/[name]-[hash][extname]'
            }
            if (/woff|woff2|eot|ttf|otf/i.test(extType)) {
              return 'fonts/[name]-[hash][extname]'
            }
            return '[ext]/[name]-[hash][extname]'
          },
          manualChunks: {
            vendor: ['vue', 'vue-router', 'pinia'],
            ui: ['element-plus'],
            charts: ['echarts']
          }
        }
      },
      // 大文件警告阈值（KB）
      chunkSizeWarningLimit: 1000
    },
    
    // CSS配置
    css: {
      preprocessorOptions: {
        scss: {
          additionalData: `@import "@/styles/variables.scss";`
        }
      }
    },
    
    // 环境变量前缀
    envPrefix: 'VITE_',
    
    // 测试配置
    test: {
      globals: true,
      environment: 'jsdom',
      setupFiles: ['./src/test/setup.ts'],
      coverage: {
        reporter: ['text', 'json', 'html'],
        exclude: ['src/test/**', '**/*.d.ts']
      }
    }
  }
})