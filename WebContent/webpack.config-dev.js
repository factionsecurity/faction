const path = require('path');
const webpack = require('webpack')
const BomPlugin = require('webpack-utf8-bom'); 

module.exports = {
	entry: {
		main: path.resolve(__dirname, './src/index.js'),
		footer: path.resolve(__dirname, './src/footer.js'),
		dashboard: path.resolve(__dirname, './src/dashboard.js'),
		overview:  path.resolve(__dirname, './src/assessment/overview.js'), 
		vulnview:  path.resolve(__dirname, './src/assessment/vulnview.js'),
		assessment_queue:  path.resolve(__dirname, './src/assessment/assessment_queue.js'),
		checklist:  path.resolve(__dirname, './src/assessment/checklist.js'),
		peerreviewedit:  path.resolve(__dirname, './src/peerreview/peerreviewedit.js'),
		assessorreviewedit:  path.resolve(__dirname, './src/peerreview/assessorreviewedit.js'),
		scheduling:  path.resolve(__dirname, './src/engagement/scheduling.js'),
		calendar:  path.resolve(__dirname, './src/calendar/calendar.js'),
		options:  path.resolve(__dirname, './src/admin/options.js'),
		cms:  path.resolve(__dirname, './src/cms/cms.js'),
		default_vulns:  path.resolve(__dirname, './src/admin/default_vulns.js'),
		users: path.resolve(__dirname, './src/admin/users.js')
	},
	output: {
		filename: '[name].js',
		//path: path.resolve(__dirname, '../target/faction/dist/js'),
		path: path.resolve(__dirname, 'dist/js'),
	},
	module: {
		rules: [
		{
			test: /\.s[ac]ss$/i,
			use: [
				// Creates `style` nodes from JS strings
				"style-loader",
				// Translates CSS into CommonJS
				"css-loader",
				// Compiles Sass to CSS
				"sass-loader",
			],
		},
		{
			test: /\.css$/i,
			use: [
				// Creates `style` nodes from JS strings
				"style-loader",
				// Translates CSS into CommonJS
				"css-loader"
			],
		},
		],
  	},
	plugins: [
		new BomPlugin(true),
		new webpack.ProvidePlugin({
			$: 'jquery',
			jQuery: 'jquery'

		})
	]
}
