module.exports = {
  presets: [[
    '@vue/app',
    {
      useBuiltIns: 'entry'
    }
  ]],
  plugins: [[
    'component',
    {
      libraryName: '@tce/tce-components',
      styleLibraryName: 'css',
      libDir: 'dist/v1.0.0'
    }
  ]]
}
