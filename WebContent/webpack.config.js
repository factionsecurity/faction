const path = require('path');
const webpack = require('webpack')
const BomPlugin = require('webpack-utf8-bom'); 
const TerserPlugin = require('terser-webpack-plugin');

module.exports = {
	entry: {
		main: path.resolve(__dirname, './src/index.js'),
		footer: path.resolve(__dirname, './src/footer.js'),
		dashboard: path.resolve(__dirname, './src/dashboard.js'),
		overview:  path.resolve(__dirname, './src/assessment/overview.js'), 
		vulnview:  path.resolve(__dirname, './src/assessment/vulnview.js'),
		assessment_queue:  path.resolve(__dirname, './src/assessment/assessment_queue.js'),
		checklist:  path.resolve(__dirname, './src/assessment/checklist.js'),
		listbootstrap:  path.resolve(__dirname, './src/assessment/listbootstrap.js'),
		peerreviewedit:  path.resolve(__dirname, './src/peerreview/peerreviewedit.js'),
		peerreview_queue:  path.resolve(__dirname, './src/peerreview/peerreview_queue.js'),
		assessorreviewedit:  path.resolve(__dirname, './src/peerreview/assessorreviewedit.js'),
		scheduling:  path.resolve(__dirname, './src/engagement/scheduling.js'),
		calendar:  path.resolve(__dirname, './src/calendar/calendar.js'),
		options:  path.resolve(__dirname, './src/admin/options.js'),
		cms:  path.resolve(__dirname, './src/cms/cms.js'),
		default_vulns:  path.resolve(__dirname, './src/admin/default_vulns.js'),
		users: path.resolve(__dirname, './src/admin/users.js'),
		verification_edit: path.resolve(__dirname, './src/remediation/verification_edit.js'),
		remediation: path.resolve(__dirname, './src/remediation/remediation.js'),
		remediation_queue: path.resolve(__dirname, './src/remediation/remediation_queue.js'),
		remediation_schedule: path.resolve(__dirname, './src/remediation/remediation_schedule.js'),
		verification: path.resolve(__dirname, './src/retests/verification.js'),
		verification_queue: path.resolve(__dirname, './src/retests/verification_queue.js'),
		ice: path.resolve(__dirname, './src/ice_patched.js'),
		templates: path.resolve(__dirname, './src/templates/templates.js'),
		assessment_stats: path.resolve(__dirname, './src/assessment/assessment_stats.js'),
		appstore: path.resolve(__dirname, './src/appstore/appstore.js'),
		install_extension: path.resolve(__dirname, './src/appstore/install_extension.js')
		
	},
	output: {
		filename: '[name].js',
		path: path.resolve(__dirname, 'dist/js'),
	},
	optimization: {
        minimize: true,
        minimizer: [
            new TerserPlugin({
                terserOptions: { output: { ascii_only: true } }
            })
        ],
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
