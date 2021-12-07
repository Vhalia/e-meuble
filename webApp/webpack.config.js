const path = require("path");
const HtmlWebpackPlugin = require('html-webpack-plugin');
const TerserPlugin = require('terser-webpack-plugin');
const webpack = require("webpack");


module.exports = {
  
  mode: "none",
  entry: "./src/index.js",
  output: {
    path: __dirname + "/dist",
    filename: "bundle.js",
  },
  optimization: {
    minimizer: [new TerserPlugin({
    chunkFilter: (chunk) => {
        // Exclude mapsjs chunk from the minification as it is already minified
        if (/mapsjs/.test(chunk.name)) return false;
        return true;
    }
    })],
  },
  devtool: "cheap-module-eval-source-map",
  devServer: {
    contentBase: path.join(__dirname, "dist"),
    port: 80,
    host: '0.0.0.0', // server to be accessible externally
    public: "localhost", // force to open localhost instead of 0.0.0.0
    open: true, // open the default browser
    historyApiFallback: true, // serve index.html instead of routes leading to no specific ressource
    proxy: {
      "/api": {
        target: "http://localhost:8080",
        pathRewrite: {'^/api' : ''}
      },
    },
  },
  module: {
    rules: [
      {
        test: /\.css$/,
        use: ["style-loader", "css-loader"],
      },
      {
        test: /\.(png|svg|jpg|gif|mp3|mpe?g)$/,
        use: ["file-loader"],
      },
      {
        test: /\.js$/,
        exclude: /node_modules/,
        use: {
          loader: "babel-loader",
          options: {
            presets: [
              [
                "@babel/preset-env",
                {
                  targets: {
                    esmodules: true,
                  },
                },
              ],
            ],
          },
        },
      },
    ],
  },
  plugins: [
    new HtmlWebpackPlugin({
      template: "./src/index.html"
    }),
    new webpack.ProvidePlugin({
      $ : 'jquery',
      jQuery : 'jquery',
      "window.jQuery" : "jquery",
      "window.$" : "jquery"
    })
  ]
};
