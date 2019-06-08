const path = require('path');

module.exports = {
  devtool: 'inline-source-map',
  entry: './src/index.ts',
  output: {
    filename: 'bundle.js',
    path: path.resolve(__dirname),
    library: 'useragentSdk',
    libraryExport: 'default',
    libraryTarget: 'commonjs2'
  },
  resolve: {
    extensions: ['.ts', '.tsx', '.js', '.node']
  },
  node: {
    fs: 'empty',
    crypto: true,
  },
  // externals: [
  //   "node-webcrypto-ossl"
  // ],
  target: 'web',
  module: {
    noParse: /node\-webcrypto\-ossl/,
    rules: [
      { test: /\.ts$/,
        use: [
          {
            loader: 'ts-loader',
            options: {
              compilerOptions: {
                lib: ['es2018']
              }
            }
          },
        ],
        exclude: [
          path.resolve(__dirname, "./node_modules/webcrypto-core",),
          path.resolve(__dirname, "./node_modules/node-webcrypto-ossl"),
        ]
      },
      { test: /\.node$/, use: 'node-loader' }
    ]
  }
};
